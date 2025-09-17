package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    // Por ahora no necesitamos métodos personalizados aquí.
    // Podrías agregar búsquedas por nombre, categoría, etc.
    // List<Producto> findByCategoria_IdCategoria(Integer idCategoria);
}