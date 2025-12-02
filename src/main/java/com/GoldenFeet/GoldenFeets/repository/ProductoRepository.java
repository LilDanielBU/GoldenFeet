package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Métodos de búsqueda estándar derivados del nombre
    List<Producto> findByDestacado(boolean destacado);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByCategoriaNombre(String nombreCategoria);

    // 💥 CORRECCIÓN CRÍTICA:
    // Utilizamos @Query para obtener las marcas distintas.
    // Esto es útil para poblar filtros en el catálogo.
    @Query("SELECT DISTINCT p.marca FROM Producto p WHERE p.marca IS NOT NULL")
    List<String> findMarcasDistintas();
}