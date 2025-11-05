package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

// --- CORRECCIÓN CRÍTICA AQUÍ ---
// Asegúrate de que el segundo tipo genérico sea 'Integer'
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByDestacado(boolean destacado);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByCategoriaNombre(String nombreCategoria);

    @Query("SELECT DISTINCT p.marca FROM Producto p WHERE p.marca IS NOT NULL")
    List<String> findMarcasDistintas();
}