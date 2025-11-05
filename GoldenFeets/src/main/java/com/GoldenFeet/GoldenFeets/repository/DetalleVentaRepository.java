package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // <-- IMPORTAR ESTO
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
// Asumo que la clave primaria de DetalleVenta es Long
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    /**
     * Elimina todos los detalles de venta asociados a un ID de producto específico.
     *
     * @param productoId El ID del producto (tipo Long, como se usa para las FKs)
     */
    // --- CORRECCIÓN CRÍTICA ---
    // @Modifying es requerido para operaciones de borrado masivo por FK
    @Modifying
    @Transactional
    void deleteByProducto_Id(Long productoId);

}