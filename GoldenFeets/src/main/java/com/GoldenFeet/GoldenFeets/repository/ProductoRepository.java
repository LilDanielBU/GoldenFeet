package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoria_Nombre(String nombreCategoria);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByDestacado(boolean destacado);
}