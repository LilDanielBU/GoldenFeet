package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;

import java.util.List;
import java.util.Optional;

public interface ProductoService {

    List<ProductoDTO> listarTodos();

    // CORRECCIÓN: El tipo de ID ahora es Long
    Optional<ProductoDTO> buscarPorId(Long id);

    List<CategoriaDTO> listarCategorias();

    List<ProductoDTO> listarDestacados();
}