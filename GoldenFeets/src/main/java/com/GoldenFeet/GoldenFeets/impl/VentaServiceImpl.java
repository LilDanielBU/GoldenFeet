package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.CrearVentaRequestDTO;
import com.GoldenFeet.GoldenFeets.dto.DetalleVentaDTO;
// import com.GoldenFeet.GoldenFeets.dto.ItemVentaDTO; // Se elimina si no se usa
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private InventarioService inventarioService;

    @Override
    public List<Venta> findAllVentas() {
        return ventaRepository.findAll();
    }

    @Override
    public Optional<Venta> findVentaById(Long id) {
        return ventaRepository.findById(id);
    }

    @Override
    public void deleteVenta(Long id) {
        ventaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public VentaResponseDTO crearVenta(CrearVentaRequestDTO requestDTO, String clienteEmail) {
        Usuario cliente = usuarioRepository.findByEmail(clienteEmail)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con email: " + clienteEmail));

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setFechaVenta(LocalDate.now());
        venta.setEstado("PROCESANDO");

        Venta ventaGuardada = ventaRepository.save(venta);

        List<DetalleVenta> detallesGuardados = new ArrayList<>();
        BigDecimal totalVenta = BigDecimal.ZERO;

        // --- CORRECCIÓN 1: Typo en el tipo de la variable del loop ---
        for (ItemVentaDTO itemDto : requestDTO.items()) {
            Producto producto = productoRepository.findById(itemDto.productoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + itemDto.productoId()));

            try {
                inventarioService.actualizarStock(producto.getId(), itemDto.cantidad(), "restar");
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(e.getMessage());
            }

            DetalleVenta detalle = new DetalleVenta(ventaGuardada, producto, itemDto.cantidad(), producto.getPrecio());
            detallesGuardados.add(detalleVentaRepository.save(detalle));
            totalVenta = totalVenta.add(detalle.getSubtotal());
        }

        // --- CORRECCIÓN 2: Se elimina el cast inválido a Set ---
        ventaGuardada.setDetallesVenta(detallesGuardados);
        ventaGuardada.setTotal(totalVenta);
        ventaGuardada.setEstado("COMPLETADO");

        return VentaResponseDTO.fromEntity(ventaRepository.save(ventaGuardada));
    }
}