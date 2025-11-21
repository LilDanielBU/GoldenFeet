package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.HistorialDTO;
import com.GoldenFeet.GoldenFeets.dto.IngresoDTO;
import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.repository.InventarioMovimientoRepository;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.service.InventarioMovimientoService; // Importación necesaria

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

        if (ingresoDTO.getCantidad() == null || ingresoDTO.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        // 💥 CORRECCIÓN: Convertimos el ID (Integer) a Long para buscar en ProductoRepository.
        Long productoIdLong = ingresoDTO.getProductoId().longValue();

        Producto producto = productoRepository.findById(productoIdLong)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + ingresoDTO.getProductoId()));

        // Actualizar el stock
        int stockAnterior = producto.getStock();
        int cantidadIngresada = ingresoDTO.getCantidad();
        int stockNuevo = stockAnterior + cantidadIngresada;
        producto.setStock(stockNuevo);
        productoRepository.save(producto);

        // Crear y guardar el movimiento
        // Asumiendo que el constructor de InventarioMovimiento establece la fecha.
        InventarioMovimiento movimiento = new InventarioMovimiento(
                producto,
                "INGRESO",
                cantidadIngresada,
                ingresoDTO.getMotivo()
        );

        inventarioMovimientoRepository.save(movimiento);
    }

    /**
     * Registra una salida (resta) de inventario.
     */
    @Transactional
    @Override
    public void registrarSalida(IngresoDTO salidaDTO) {

        if (salidaDTO.getCantidad() == null || salidaDTO.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad de salida debe ser al menos 1.");
        }

        // 💥 CORRECCIÓN: Convertimos el ID (Integer) a Long para buscar en ProductoRepository.
        Long productoIdLong = salidaDTO.getProductoId().longValue();

        // 1. Buscar el producto
        Producto producto = productoRepository.findById(productoIdLong)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + salidaDTO.getProductoId()));

        // 2. Verificar y actualizar el stock
        int stockAnterior = producto.getStock();
        int cantidadRetirada = salidaDTO.getCantidad();
        int stockNuevo = stockAnterior - cantidadRetirada;

        if (stockNuevo < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo. Stock actual: " + stockAnterior + ".");
        }

        producto.setStock(stockNuevo);
        productoRepository.save(producto); // Guarda el producto con el stock actualizado

        // 3. Crear el registro del movimiento
        InventarioMovimiento movimiento = new InventarioMovimiento(
                producto,
                "SALIDA", // Tipo de movimiento
                cantidadRetirada,
                salidaDTO.getMotivo()
        );

        // 4. Guardar el movimiento en el historial
        inventarioMovimientoRepository.save(movimiento);
    }


    /**
     * Obtiene el historial de un producto.
     */
    @Override
    @Transactional(readOnly = true)
    public List<HistorialDTO> getHistorialPorProducto(Integer productoId) {

        // Ya está corregido para Long
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