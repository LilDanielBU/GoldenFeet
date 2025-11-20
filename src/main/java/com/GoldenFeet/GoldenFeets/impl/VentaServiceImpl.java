package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.*;
import com.GoldenFeet.GoldenFeets.entity.*;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import com.GoldenFeet.GoldenFeets.repository.VentaRepository;
import com.GoldenFeet.GoldenFeets.service.EntregaService;
import com.GoldenFeet.GoldenFeets.service.VentaService;
import com.GoldenFeet.GoldenFeets.repository.InventarioMovimientoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EntregaService entregaService;
    private final InventarioMovimientoRepository inventarioMovimientoRepository;


    /**
     * Crea una nueva venta, registra los detalles, descuenta el stock de los productos
     * y crea una entrada de entrega.
     */
    @Override
    @Transactional
    public VentaResponseDTO crearVenta(CrearVentaRequestDTO request, String clienteEmail) {

        Usuario cliente = usuarioRepository.findByEmail(clienteEmail)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con email: " + clienteEmail));

        Venta nuevaVenta = new Venta();
        nuevaVenta.setCliente(cliente);
        nuevaVenta.setFechaVenta(LocalDate.now());
        nuevaVenta.setEstado("PENDIENTE_PAGO");
        nuevaVenta.setDireccionEnvio(request.getDireccion());
        nuevaVenta.setCiudadEnvio(request.getCiudad() + ", " + request.getDepartamento());

        // 🚨 CORRECCIÓN 1: Establecer la localidad en la Venta
        // Asumiendo que CrearVentaRequestDTO tiene getLocalidad()
        nuevaVenta.setLocalidad(request.getLocalidad());

        nuevaVenta.setMetodoPago(request.getMetodoPago());

        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal totalVenta = BigDecimal.ZERO;

        // Itera sobre los ítems para validar stock y preparar los detalles
        for (ItemVentaDTO itemDTO : request.getItems()) {

            Producto producto = productoRepository.findById(Long.valueOf(itemDTO.productoId()))
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + itemDTO.productoId()));

            // 1. VALIDACIÓN DE STOCK
            if (producto.getStock() < itemDTO.cantidad()) {
                throw new IllegalStateException("Stock insuficiente para: " + producto.getNombre() + ". Stock actual: " + producto.getStock());
            }

            // 2. DESCUENTO DE STOCK CRÍTICO (DENTRO DE LA TRANSACCIÓN)
            int nuevaCantidadStock = producto.getStock() - itemDTO.cantidad();
            producto.setStock(nuevaCantidadStock);

            // 3. ACTUALIZAR EL PRODUCTO
            productoRepository.save(producto);

            // 4. REGISTRAR MOVIMIENTO HISTÓRICO
            InventarioMovimiento movimiento = new InventarioMovimiento();
            movimiento.setProducto(producto);
            movimiento.setCantidad(itemDTO.cantidad());
            movimiento.setTipoMovimiento("SALIDA");
            movimiento.setMotivo("Venta");
            movimiento.setFecha(LocalDateTime.now());
            inventarioMovimientoRepository.save(movimiento);


            // 5. Crear el detalle de la venta
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(itemDTO.cantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            BigDecimal subtotal = producto.getPrecio().multiply(new BigDecimal(itemDTO.cantidad()));
            detalle.setSubtotal(subtotal);
            detalle.setVenta(nuevaVenta);
            detalles.add(detalle);
            totalVenta = totalVenta.add(subtotal);
        }

        // 6. Finalizar y guardar la Venta
        nuevaVenta.setTotal(totalVenta);
        nuevaVenta.setDetallesVenta(detalles);
        Venta ventaGuardada = ventaRepository.save(nuevaVenta);

        // 7. Crear el registro de Entrega
        Entrega nuevaEntrega = new Entrega();
        nuevaEntrega.setVenta(ventaGuardada);
        nuevaEntrega.setEstado("ASIGNAR_DISTRIBUIDOR");
        nuevaEntrega.setFechaCreacion(LocalDateTime.now());

        // 🚨 CORRECCIÓN 2: Establecer la localidad en la Entrega
        // Esto es necesario para que aparezca en el Panel de Entregas y para el filtro.
        nuevaEntrega.setLocalidad(request.getLocalidad());

        entregaService.guardar(nuevaEntrega);

        // 8. Retornar DTO de respuesta
        return convertirAVentaResponseDTO(ventaGuardada);
    }

    // =========================================================
    // IMPLEMENTACIONES DE LA INTERFAZ (VentaService)
    // =========================================================

    @Override
    public List<VentaResponseDTO> buscarVentasPorCliente(Long idCliente) {
        return ventaRepository.findByCliente_IdUsuario(idCliente.intValue()).stream()
                .map(this::convertirAVentaResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VentaResponseDTO> findAllVentas() {
        return ventaRepository.findAll().stream()
                .map(this::convertirAVentaResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Venta> findVentaById(Long id) {
        return ventaRepository.findById(id);
    }

    @Override
    public List<Venta> obtenerTodasLasVentas() {
        return ventaRepository.findAll();
    }

    @Override
    public List<Venta> obtenerVentasPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        return ventaRepository.findByFechaVentaBetween(fechaInicio, fechaFin);
    }

    @Override
    public Venta obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Venta guardarVenta(Venta venta) {
        return ventaRepository.save(venta);
    }

    @Override
    @Transactional
    public void eliminarVenta(Long id) {
        if (!ventaRepository.existsById(id)) {
            throw new EntityNotFoundException("Venta no encontrada con ID: " + id);
        }
        ventaRepository.deleteById(id);
    }

    // =========================================================
    // MÉTODOS PRIVADOS

    private VentaResponseDTO convertirAVentaResponseDTO(Venta venta) {
        return VentaResponseDTO.fromEntity(venta);
    }
}