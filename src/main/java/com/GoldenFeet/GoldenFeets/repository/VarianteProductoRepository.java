package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.VarianteProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VarianteProductoRepository extends JpaRepository<VarianteProducto, Long> {

    // 1. CORRECCIÓN: Para obtener todas las variantes dado el ID del Producto.
    // Asume que la entidad VarianteProducto tiene un campo de relación llamado 'producto'.
    List<VarianteProducto> findByProductoId(Long productoId);

    // 2. CORRECCIÓN: Para contar cuántas variantes quedan para un Producto.
    // Necesario para decidir si eliminar el producto padre.
    Long countByProductoId(Long productoId);
}