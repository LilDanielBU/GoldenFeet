package com.GoldenFeet.GoldenFeets.service; // <-- CORREGIDO: El paquete de implementación debe ser 'impl'

import com.GoldenFeet.GoldenFeets.dto.HistorialDTO;
import com.GoldenFeet.GoldenFeets.dto.IngresoDTO;
import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.repository.InventarioMovimientoRepository;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.service.InventarioMovimientoService; // Importa la interfaz

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de movimientos de inventario.
 * Se encarga de coordinar los repositorios de Producto e InventarioMovimiento.
 */
@Service
public class InventarioMovimientoServiceImpl implements InventarioMovimientoService {

    private final InventarioMovimientoRepository inventarioMovimientoRepository;
    private final ProductoRepository productoRepository;

    @Autowired
    public InventarioMovimientoServiceImpl(InventarioMovimientoRepository inventarioMovimientoRepository,
                                           ProductoRepository productoRepository) {
        this.inventarioMovimientoRepository = inventarioMovimientoRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Registra un ingreso de inventario.
     */
    @Override
    @Transactional
    public void registrarIngreso(IngresoDTO ingresoDTO) {
        if (ingresoDTO.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        // Buscar el producto (ID es Integer)
        Producto producto = productoRepository.findById(ingresoDTO.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + ingresoDTO.getProductoId()));

        // Actualizar el stock
        int stockAnterior = producto.getStock();
        int stockNuevo = stockAnterior + ingresoDTO.getCantidad();
        producto.setStock(stockNuevo);
        productoRepository.save(producto);

        // Crear y guardar el movimiento
        InventarioMovimiento movimiento = new InventarioMovimiento(
                producto,
                "INGRESO",
                ingresoDTO.getCantidad(),
                ingresoDTO.getMotivo()
        );

        inventarioMovimientoRepository.save(movimiento);
    }

    /**
     * Obtiene el historial de un producto.
     */
    @Override
    @Transactional(readOnly = true)
    public List<HistorialDTO> getHistorialPorProducto(Integer productoId) {

        // 1. CORRECCIÓN CRÍTICA: Usamos el método correcto findByProducto_Id,
        // asumiendo que la ordenación (OrderByFechaDesc) se maneja en el DTO o en el cliente.
        List<InventarioMovimiento> movimientos = inventarioMovimientoRepository
                .findByProducto_Id(Long.valueOf(productoId));

        // 2. Convertir la lista de Entidades a DTOs
        return movimientos.stream()
                .map(this::convertirAHistorialDTO)
                .collect(Collectors.toList());
    }

    /**
     * Método privado para convertir una Entidad a un DTO.
     */
    private HistorialDTO convertirAHistorialDTO(InventarioMovimiento movimiento) {
        return new HistorialDTO(
                movimiento.getFecha(),
                movimiento.getTipoMovimiento(),
                movimiento.getCantidad(),
                movimiento.getMotivo()
        );
    }
}