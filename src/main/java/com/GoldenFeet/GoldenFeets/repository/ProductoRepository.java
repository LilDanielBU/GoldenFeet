package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Necesitas esta importación
import org.springframework.stereotype.Repository;

import java.util.List;

// Asumimos JpaRepository<Producto, Long> para la coherencia que encontramos
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Métodos de búsqueda normales
    List<Producto> findByDestacado(boolean destacado);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByCategoriaNombre(String nombreCategoria);

    // 💥 CORRECCIÓN CRÍTICA: Se añade @Query para métodos que no son de propiedad.
    // Usamos JPQL para seleccionar los valores distintos del campo 'marca'.
    @Query("SELECT DISTINCT p.marca FROM Producto p WHERE p.marca IS NOT NULL")
    List<String> findMarcasDistintas();
}