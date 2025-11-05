package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- Asegúrate de importar @Query
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
//                                                     ¡CORRECTO! ->
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // --- INICIO DE CORRECCIÓN (MÉTODOS FALTANTES) ---

    /**
     * Spring Data JPA crea esta consulta automáticamente.
     * Busca Productos donde el nombre de la Categoria (entidad relacionada) coincida.
     * "Categoria" es el campo 'categoria' en Producto.
     * "Nombre" es el campo 'nombre' en Categoria.
     */
    List<Producto> findByCategoriaNombre(String nombreCategoria);

    /**
     * Este método también faltaba y es llamado por el Service.
     * Usamos una consulta JPQL personalizada para obtener marcas únicas.
     */
    @Query("SELECT DISTINCT p.marca FROM Producto p WHERE p.marca IS NOT NULL ORDER BY p.marca ASC")
    List<String> findMarcasDistintas();

    // --- FIN DE CORRECCIÓN ---


    // --- Métodos que probablemente ya tenías ---
    List<Producto> findByDestacado(boolean destacado);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}