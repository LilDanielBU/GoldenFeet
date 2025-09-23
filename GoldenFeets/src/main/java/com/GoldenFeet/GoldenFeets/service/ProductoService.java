package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoCreateDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoUpdateDTO; // <-- Importar el DTO de actualización
import com.GoldenFeet.GoldenFeets.entity.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoService {

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

    /**
     * Crea un nuevo producto en la base de datos a partir de un DTO.
     * @param productoDTO El DTO con la información del producto a crear.
     * @return La entidad del producto guardado.
     */
    Producto crearProducto(ProductoCreateDTO productoDTO);

    /**
     * Actualiza un producto existente en la base de datos.
     * @param id El ID del producto a actualizar.
     * @param productoDTO El DTO con la nueva información.
     * @return La entidad del producto actualizado.
     */
    Producto actualizarProducto(Integer id, ProductoUpdateDTO productoDTO); // <-- MÉTODO NUEVO PARA EDITAR

    /**
     * Elimina un producto de la base de datos por su ID.
     * @param id El ID del producto a eliminar.
     */
    void eliminarProducto(Integer id); // <-- MÉTODO PARA ELIMINAR
}