package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import java.util.List;
import java.util.Optional;

public interface ProductoService {

    List<ProductoDTO> listarTodos();

    // --- CORRECCIÓN: Cambiado de Long a Integer ---
    Optional<ProductoDTO> buscarPorId(Integer id);

    List<CategoriaDTO> listarCategorias();

    List<ProductoDTO> listarDestacados();

    List<ProductoDTO> buscarPorNombre(String nombre);

    List<ProductoDTO> listarPorCategoria(String nombreCategoria);

    List<ProductoDTO> filtrarProductos(String categoria, Double precioMax, List<String> marcas);

    List<String> listarMarcasDistintas();

    // --- CORRECCIÓN: Cambiado de List<Long> a List<Integer> ---
    List<ProductoDTO> listarPorIds(List<Integer> ids);

    List<ProductoDTO> obtenerProductosRecientes(int cantidad);
}