package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.CrearVentaRequestDTO;
import com.GoldenFeet.GoldenFeets.dto.DetalleVentaDTO;
import com.GoldenFeet.GoldenFeets.dto.ItemVentaDTO;
import com.GoldenFeet.GoldenFeets.dto.VentaResponseDTO;
import com.GoldenFeet.GoldenFeets.entity.DetalleVenta;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.entity.Venta;
import com.GoldenFeet.GoldenFeets.repository.DetalleVentaRepository;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import com.GoldenFeet.GoldenFeets.repository.VentaRepository;
import com.GoldenFeet.GoldenFeets.service.InventarioService;
import com.GoldenFeet.GoldenFeets.service.VentaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private InventarioService inventarioService;

    // Note: DetalleVentaRepository is not needed here if using CascadeType.ALL

    @Override
    @Transactional
    public VentaResponseDTO crearVenta(CrearVentaRequestDTO requestDTO, String clienteEmail) {
        Usuario cliente = usuarioRepository.findByEmail(clienteEmail)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con email: " + clienteEmail));

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setFechaVenta(LocalDate.now());
        venta.setEstado("PROCESANDO");

        BigDecimal totalVenta = BigDecimal.ZERO;

        // Itera sobre los DTOs para procesar cada ítem
        for (ItemVentaDTO itemDto : requestDTO.items()) { // <-- CORRECCIÓN DE TYPO
            Producto producto = productoRepository.findById(itemDto.productoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + itemDto.productoId()));

            // Descuenta el stock del inventario
            inventarioService.actualizarStock(producto.getId(), itemDto.cantidad(), "restar");

            // Crea el nuevo detalle
            DetalleVenta detalle = new DetalleVenta(venta, producto, itemDto.cantidad(), producto.getPrecio());

            // --- CORRECCIÓN ORPHAN REMOVAL ---
            // Añade el detalle a la lista de la venta usando el método de ayuda.
            // Esto mantiene la colección original y evita el error.
            venta.addDetalle(detalle);

            totalVenta = totalVenta.add(detalle.getSubtotal());
        }

        // Actualiza el total y el estado de la venta
        venta.setTotal(totalVenta);
        venta.setEstado("COMPLETADO");

        // Guarda la venta. Gracias a CascadeType.ALL, los nuevos detalles se guardarán automáticamente.
        Venta ventaFinal = ventaRepository.save(venta);
        return VentaResponseDTO.fromEntity(ventaFinal);
    }

    @Override
    public List<VentaResponseDTO> buscarVentasPorCliente(Long idCliente) {
        return ventaRepository.findByClienteIdUsuario(idCliente).stream()
                .map(VentaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<VentaResponseDTO> findAllVentas() {
        return ventaRepository.findAll().stream()
                .map(VentaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<VentaResponseDTO> findVentaById(Long id) {
        return ventaRepository.findById(id)
                .map(VentaResponseDTO::fromEntity);
    }
}