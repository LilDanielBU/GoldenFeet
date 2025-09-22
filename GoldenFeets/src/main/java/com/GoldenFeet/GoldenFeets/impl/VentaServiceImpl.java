package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.*;
import com.GoldenFeet.GoldenFeets.entity.*;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import com.GoldenFeet.GoldenFeets.repository.VentaRepository;
import com.GoldenFeet.GoldenFeets.service.EntregaService; // <-- NUEVO: Importar el servicio de Entrega
import com.GoldenFeet.GoldenFeets.service.VentaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime; // <-- NUEVO: Importar para la fecha de creación de la entrega
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EntregaService entregaService; // <-- NUEVO: Inyectar el servicio de Entrega

    @Override
    @Transactional
    public VentaResponseDTO crearVenta(CrearVentaRequestDTO request, String clienteEmail) {
        Usuario cliente = usuarioRepository.findByEmail(clienteEmail)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con email: " + clienteEmail));

        Venta nuevaVenta = new Venta();
        nuevaVenta.setCliente(cliente);
        nuevaVenta.setFechaVenta(LocalDate.now());
        nuevaVenta.setEstado("COMPLETADA");

        nuevaVenta.setDireccionEnvio(request.direccion());
        nuevaVenta.setCiudadEnvio(request.ciudad() + ", " + request.departamento());
        nuevaVenta.setMetodoPago(request.metodoPago());

        Set<DetalleVenta> detalles = new HashSet<>();
        BigDecimal totalVenta = BigDecimal.ZERO;

        for (ItemVentaDTO itemDTO : request.items()) {
            Producto producto = productoRepository.findById(itemDTO.productoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + itemDTO.productoId()));

            if (producto.getStock() < itemDTO.cantidad()) {
                throw new IllegalStateException("Stock insuficiente para: " + producto.getNombre());
            }
            producto.setStock(producto.getStock() - itemDTO.cantidad());

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

        nuevaVenta.setTotal(totalVenta);
        nuevaVenta.setDetallesVenta(detalles);
        Venta ventaGuardada = ventaRepository.save(nuevaVenta);

        // --- INICIO DE LA LÓGICA DE ENTREGA AÑADIDA ---
        Entrega nuevaEntrega = new Entrega();
        nuevaEntrega.setVenta(ventaGuardada); // Vinculamos la entrega a la venta recién creada
        nuevaEntrega.setEstado("PENDIENTE"); // El estado inicial de toda entrega
        nuevaEntrega.setFechaCreacion(LocalDateTime.now()); // Guardamos la fecha y hora de creación
        nuevaEntrega.setDistribuidor(null); // El gerente lo asignará después

        // Usamos el servicio de entrega para guardar la nueva entidad en la base de datos
        entregaService.guardar(nuevaEntrega);
        // --- FIN DE LA LÓGICA DE ENTREGA AÑADIDA ---

        return convertirAVentaResponseDTO(ventaGuardada);
    }

    @Override
    public List<VentaResponseDTO> buscarVentasPorCliente(Integer idCliente) {
        return ventaRepository.findByCliente_IdUsuario(idCliente).stream()
                .map(this::convertirAVentaResponseDTO)
                .collect(Collectors.toList());
    }

    // --- Métodos adicionales para el AdminController (SIN CAMBIOS) ---
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

    // --- Método de conversión para uso interno (SIN CAMBIOS) ---
    private VentaResponseDTO convertirAVentaResponseDTO(Venta venta) {
        List<DetalleVentaDTO> detallesDTO = venta.getDetallesVenta().stream()
                .map(detalle -> new DetalleVentaDTO(
                        detalle.getProducto().getId(),
                        detalle.getProducto().getNombre(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()
                )).collect(Collectors.toList());

        return new VentaResponseDTO(
                venta.getIdVenta(),
                venta.getFechaVenta(),
                venta.getTotal(),
                venta.getEstado(),
                venta.getCliente().getEmail(),
                detallesDTO
        );
    }
}