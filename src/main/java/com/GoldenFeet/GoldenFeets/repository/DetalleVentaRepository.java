package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    /**
     * Elimina todos los detalles de venta asociados a un ID de producto específico.
     *
     * @param productoId El ID del producto cuyos detalles de venta se eliminarán.
     */
    @Transactional
    void deleteByProducto_Id(Integer productoId);

}