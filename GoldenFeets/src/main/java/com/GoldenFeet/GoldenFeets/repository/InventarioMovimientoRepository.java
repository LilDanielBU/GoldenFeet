package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // <-- ¡Asegúrate de importar List!

public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Long> {

    /**
     * Busca todos los movimientos de inventario asociados a un ID de producto.
     *
     * @param productoId El ID del producto (debe ser Long, para coincidir con Producto.id)
     * @return Una lista de movimientos para ese producto.
     */
    // --- CORRECCIÓN ---
    // El método se llama findByProducto_Id para que Spring sepa que es
    // el campo 'id' dentro de la entidad 'producto'.
    // El tipo debe ser Long.
    List<InventarioMovimiento> findByProducto_Id(Long productoId);


    /**
     * Elimina todos los movimientos de inventario asociados a un ID de producto.
     *
     * @param productoId El ID del producto (debe ser Long)
     */
    // --- MÉTODO AÑADIDO (NECESARIO PARA EL SERVICE) ---
    void deleteByProducto_Id(Long productoId);

}