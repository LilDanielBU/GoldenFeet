package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // <-- Importación necesaria
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Collection;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria")
    @Override
    List<Producto> findAll();

    @Query("SELECT p FROM Producto p ORDER BY p.id DESC")
    List<Producto> findProductosRecientes(Pageable pageable);


    List<Producto> findByCategoria_Nombre(String nombreCategoria);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByIdIn(Collection<Long> ids);

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.destacado = :destacado")
    List<Producto> findByDestacado(@Param("destacado") boolean destacado);

    @Query("SELECT DISTINCT p.marca FROM Producto p ORDER BY p.marca")
    List<String> findDistinctMarcas();


    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria c WHERE " +
            "(:categoria IS NULL OR c.nombre = :categoria) AND " +
            "(:precioMax IS NULL OR p.precio <= :precioMax) AND " +
            "(:marcas IS NULL OR p.marca IN :marcas)")
    List<Producto> filtrarProductos(
            @Param("categoria") String categoria,
            @Param("precioMax") Double precioMax,
            @Param("marcas") List<String> marcas
    );
}