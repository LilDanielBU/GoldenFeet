package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // <-- ¡Asegúrate de importar List!

public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Long> {

    // --- AÑADE ESTE MÉTODO ---
    /**
     * Spring Data JPA crea esta consulta automáticamente.
     * 1. Busca por el campo 'producto' (que es una entidad Producto).
     * 2. Específicamente, por el 'Id' dentro de esa entidad 'producto'.
     * 3. Ordena por el campo 'fecha' en orden 'Desc' (descendente).
     *
     * @param productoId El ID del producto (que es Integer)
     * @return Una lista de movimientos para ese producto.
     */


    List<InventarioMovimiento> findByProductoIdOrderByFechaDesc(Integer productoId);
}