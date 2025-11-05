package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.*;
import com.GoldenFeet.GoldenFeets.entity.*;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import com.GoldenFeet.GoldenFeets.repository.VentaRepository;
import com.GoldenFeet.GoldenFeets.service.EntregaService;
import com.GoldenFeet.GoldenFeets.service.VentaService;
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

        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal totalVenta = BigDecimal.ZERO;

        for (ItemVentaDTO itemDTO : request.items()) {
            Producto producto = productoRepository.findById(itemDTO.productoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + itemDTO.productoId()));

            if (producto.getStock() < itemDTO.cantidad()) {
                throw new IllegalStateException("Stock insuficiente para: " + producto.getNombre());
            }
            // Lógica de stock a futuro

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

        Entrega nuevaEntrega = new Entrega();
        nuevaEntrega.setVenta(ventaGuardada);
        nuevaEntrega.setEstado("PENDIENTE");
        nuevaEntrega.setFechaCreacion(LocalDateTime.now());
        entregaService.guardar(nuevaEntrega);

        return convertirAVentaResponseDTO(ventaGuardada);
    }


    public List<VentaResponseDTO> buscarVentasPorCliente(Integer idCliente) {
        return ventaRepository.findByCliente_IdUsuario(idCliente).stream()
                .map(this::convertirAVentaResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VentaResponseDTO> buscarVentasPorCliente(Long idCliente) {
        return buscarVentasPorCliente(idCliente.intValue());
    }

    @Override
    public List<VentaResponseDTO> findAllVentas() {
        return List.of();
    }

    @Override
    public Optional<Venta> findVentaById(Long id) {
        return ventaRepository.findById(id);
    }


    public List<Venta> obtenerTodasLasVentas() {
        return ventaRepository.findAll();
    }


    public List<Venta> obtenerVentasPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        return ventaRepository.findByFechaVentaBetween(fechaInicio, fechaFin);
    }


    public Venta obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }


    @Transactional
    public Venta guardarVenta(Venta venta) {
        return ventaRepository.save(venta);
    }


    @Transactional
    public void eliminarVenta(Long id) {
        if (!ventaRepository.existsById(id)) {
            throw new EntityNotFoundException("Venta no encontrada con ID: " + id);
        }
        ventaRepository.deleteById(id);
    }

    // --- MÉTODO DE CONVERSIÓN ACTUALIZADO ---
    private VentaResponseDTO convertirAVentaResponseDTO(Venta venta) {
        // Ahora simplemente usamos el método estático del DTO
        return VentaResponseDTO.fromEntity(venta);
    }
}