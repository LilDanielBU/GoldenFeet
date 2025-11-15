package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    // Este método es útil para buscar, lo puedes conservar.
    Optional<Inventario> findByProducto_Id(Integer productoId);

    /**
     * MÉTODO AÑADIDO PARA ELIMINAR
     * Elimina el registro de inventario asociado a un ID de producto específico.
     * @param productoId El ID del producto.
     */
    @Transactional
    void deleteByProducto_Id(Integer productoId);

}