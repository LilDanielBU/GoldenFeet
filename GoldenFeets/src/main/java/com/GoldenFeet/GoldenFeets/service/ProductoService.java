package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;

import java.util.List;
import java.util.Optional;

public interface ProductoService {

    List<ProductoDTO> listarTodos();

    Optional<ProductoDTO> buscarPorId(Long id);

    List<CategoriaDTO> listarCategorias();

    List<ProductoDTO> listarDestacados();

    List<ProductoDTO> buscarPorNombre(String nombre);

    List<ProductoDTO> listarPorCategoria(String nombreCategoria);

    List<ProductoDTO> filtrarProductos(String categoria, Double precioMax, List<String> marcas);
    List<String> listarMarcasDistintas();
    List<ProductoDTO> listarPorIds(List<Long> ids);
    List<ProductoDTO> obtenerProductosRecientes(int limit);
}