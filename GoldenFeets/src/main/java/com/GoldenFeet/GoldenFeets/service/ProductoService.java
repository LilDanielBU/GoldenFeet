package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import java.util.List;
import java.util.Optional;

public interface ProductoService {

    /**
     * Devuelve una lista de todos los productos disponibles.
     * @return una lista de ProductoDTO.
     */
    List<ProductoDTO> listarTodos();

    /**
     * Busca un producto por su ID.
     * @param id el ID del producto.
     * @return un Optional con el ProductoDTO si se encuentra, o un Optional vacío.
     */
    Optional<ProductoDTO> buscarPorId(Integer id);

    /**
     * Devuelve una lista de todas las categorías.
     * @return una lista de CategoriaDTO.
     */
    List<CategoriaDTO> listarCategorias();
}