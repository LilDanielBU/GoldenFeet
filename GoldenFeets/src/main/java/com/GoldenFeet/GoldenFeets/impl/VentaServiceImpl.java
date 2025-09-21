package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.*;
import com.GoldenFeet.GoldenFeets.entity.*;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import com.GoldenFeet.GoldenFeets.repository.VentaRepository;
import com.GoldenFeet.GoldenFeets.service.VentaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Override
    @Transactional
    public VentaResponseDTO crearVenta(CrearVentaRequestDTO request, String clienteEmail) {
        Usuario cliente = usuarioRepository.findByEmail(clienteEmail)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con email: " + clienteEmail));

        Venta nuevaVenta = new Venta();
        nuevaVenta.setCliente(cliente);
        nuevaVenta.setFechaVenta(LocalDate.now());
        nuevaVenta.setEstado("COMPLETADA");

        // --- ¡NUEVO! Guardamos los datos de envío y pago en la entidad ---
        nuevaVenta.setDireccionEnvio(request.direccion());
        nuevaVenta.setCiudadEnvio(request.ciudad() + ", " + request.departamento());
        nuevaVenta.setMetodoPago(request.metodoPago());
        // --- FIN DE LA SECCIÓN NUEVA ---

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

        return convertirAVentaResponseDTO(ventaGuardada);
    }

    @Override
    public List<VentaResponseDTO> buscarVentasPorCliente(Integer idCliente) {
        return ventaRepository.findByCliente_IdUsuario(idCliente).stream()
                .map(this::convertirAVentaResponseDTO)
                .collect(Collectors.toList());
    }

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