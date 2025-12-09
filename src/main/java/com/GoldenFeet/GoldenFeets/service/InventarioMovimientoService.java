package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.HistorialDTO;
import com.GoldenFeet.GoldenFeets.dto.IngresoDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface InventarioMovimientoService {

    /**
     * Registra un nuevo ingreso de stock para un producto.
     * Esto actualiza el stock en la entidad Producto y
     * guarda un registro en InventarioMovimiento.
     */
    void registrarIngreso(IngresoDTO ingresoDTO);

    @Transactional
    void registrarSalida(IngresoDTO salidaDTO);

    /**
     * Obtiene el historial de movimientos de un producto específico.
     *
     * @param productoId El ID del producto a consultar.
     * @return Una lista de DTOs con la información del historial.
     */
    List<HistorialDTO> getHistorialPorProducto(Integer productoId);

}