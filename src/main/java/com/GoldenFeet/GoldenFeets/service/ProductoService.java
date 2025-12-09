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

    // ==========================================
    // MÉTODOS DE CONSULTA (DTOs y Entidades)
    // ==========================================

    @Transactional(readOnly = true)
    Collection<Producto> listarProductos();

    @Transactional(readOnly = true)
    double calcularValorTotalInventario();

    List<ProductoDTO> listarTodos();

    // ID primario usando Long, consistente con JPA.
    Optional<ProductoDTO> buscarPorId(Long id);

    // --- NUEVO MÉTODO CRÍTICO PARA EL MODAL ---
    ProductoDTO obtenerProductoConVariantes(Long id);

    List<CategoriaDTO> listarCategorias();

    List<ProductoDTO> listarDestacados();

    List<ProductoDTO> buscarPorNombre(String nombre);

    List<ProductoDTO> listarPorCategoria(String nombreCategoria);

    List<ProductoDTO> filtrarProductos(String categoria, Double precioMax, List<String> marcas);

    List<String> listarMarcasDistintas();

    // Lista de IDs primarios usando Long.
    List<ProductoDTO> listarPorIds(List<Long> ids);

    List<ProductoDTO> obtenerProductosRecientes(int cantidad);

    // ==========================================
    // MÉTODOS DE GESTIÓN (Crear, Editar, Eliminar)
    // ==========================================

    Producto crearProducto(ProductoCreateDTO productoDTO);

    // ID primario usando Long.
    Producto actualizarProducto(Long id, ProductoUpdateDTO productoDTO);

    // ID primario usando Long.
    void eliminarProducto(Long id);

    // --- CORRECCIÓN: Método para eliminar la VARIANTE individual ---
    void eliminarVariante(Long id);
}