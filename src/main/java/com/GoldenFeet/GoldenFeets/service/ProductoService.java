package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoCreateDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoUpdateDTO;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductoService {

    @Transactional(readOnly = true)
    Collection<Producto> listarProductos();

    @Transactional(readOnly = true)
    double calcularValorTotalInventario();

    List<ProductoDTO> listarTodos();

    Optional<ProductoDTO> buscarPorId(Integer id);

    List<CategoriaDTO> listarCategorias();

    List<ProductoDTO> listarDestacados();

    List<ProductoDTO> buscarPorNombre(String nombre);

    List<ProductoDTO> listarPorCategoria(String nombreCategoria);

    List<ProductoDTO> filtrarProductos(String categoria, Double precioMax, List<String> marcas);

    List<String> listarMarcasDistintas();

    List<ProductoDTO> listarPorIds(List<Integer> ids);

    List<ProductoDTO> obtenerProductosRecientes(int cantidad);

    Producto crearProducto(ProductoCreateDTO productoDTO);

    Producto actualizarProducto(Integer id, ProductoUpdateDTO productoDTO);

    void eliminarProducto(Integer id);
}