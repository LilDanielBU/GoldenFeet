package com.GoldenFeet.GoldenFeets.service; // <-- Corregí el paquete (estaba en 'service')

import com.GoldenFeet.GoldenFeets.dto.HistorialDTO;
import com.GoldenFeet.GoldenFeets.dto.IngresoDTO;
import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.repository.InventarioMovimientoRepository;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.service.InventarioMovimientoService;

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
@Service // Le dice a Spring que esta clase es un Servicio
public class InventarioMovimientoServiceImpl implements InventarioMovimientoService {

    // Inyectamos los repositorios que necesitamos
    private final InventarioMovimientoRepository inventarioMovimientoRepository;
    private final ProductoRepository productoRepository; // Asumo que tienes este repositorio

    @Autowired
    public InventarioMovimientoServiceImpl(InventarioMovimientoRepository inventarioMovimientoRepository,
                                           ProductoRepository productoRepository) {
        this.inventarioMovimientoRepository = inventarioMovimientoRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Registra un ingreso de inventario.
     *
     * @Transactional asegura que AMBAS operaciones (actualizar stock y guardar historial)
     * se completen con éxito. Si una falla, NINGUNA se guarda.
     * Esto mantiene tu base de datos consistente.
     */
    @Override
    @Transactional
    public void registrarIngreso(IngresoDTO ingresoDTO) {
        // 1. Validar que la cantidad sea positiva
        if (ingresoDTO.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        // 2. Buscar el producto
        // Esta línea ahora funciona porque 'ingresoDTO.getProductoId()' devuelve un 'Integer'
        Producto producto = productoRepository.findById(ingresoDTO.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + ingresoDTO.getProductoId()));

        // 3. Actualizar el stock del producto
        int stockAnterior = producto.getStock();
        int stockNuevo = stockAnterior + ingresoDTO.getCantidad();
        producto.setStock(stockNuevo);
        productoRepository.save(producto); // Guarda el producto con el stock actualizado

        // 4. Crear el registro del movimiento
        InventarioMovimiento movimiento = new InventarioMovimiento(
                producto,
                "INGRESO",
                ingresoDTO.getCantidad(), // La cantidad que se movió (positiva)
                ingresoDTO.getMotivo()
        );

        // 5. Guardar el movimiento en el historial
        inventarioMovimientoRepository.save(movimiento);
    }

    /**
     * Obtiene el historial de un producto.
     */
    @Override
    @Transactional(readOnly = true) // readOnly = true optimiza la consulta, ya que no modificará datos
    // CAMBIO PRINCIPAL AQUÍ: Se cambió 'Long productoId' por 'Integer productoId'
    public List<HistorialDTO> getHistorialPorProducto(Integer productoId) {

        // 1. Buscar todos los movimientos usando el método que creamos en el repositorio
        // Esta llamada ahora pasa un 'Integer', que coincide con el repositorio
        List<InventarioMovimiento> movimientos = inventarioMovimientoRepository
                .findByProductoIdOrderByFechaDesc(productoId);

        // 2. Convertir la lista de Entidades (InventarioMovimiento)
        //    a una lista de DTOs (HistorialDTO) para enviarla al frontend.
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