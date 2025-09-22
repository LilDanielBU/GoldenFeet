package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.entity.Categoria;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    public List<ProductoDTO> listarTodos() {
        return productoRepository.findAll().stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductoDTO> buscarPorId(Long id) {
        return productoRepository.findById(id)
                .map(this::convertirAProductoDTO);
    }

    // --- MÉTODO CORREGIDO ---
    @Override
    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(categoria -> new CategoriaDTO( // La lógica de conversión se mueve aquí
                        categoria.getIdCategoria(),
                        categoria.getNombre(),
                        categoria.getDescripcion(),
                        categoria.getImagenUrl()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> listarDestacados() {
        return productoRepository.findByDestacado(true).stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> listarPorCategoria(String nombreCategoria) {
        return productoRepository.findByCategoria_Nombre(nombreCategoria).stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> obtenerProductosRecientes(int cantidad) {
        Pageable pageable = PageRequest.of(0, cantidad, Sort.by("id").descending());
        return productoRepository.findAll(pageable).getContent().stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listarMarcasDistintas() {
        return productoRepository.findDistinctMarcas();
    }

    @Override
    public List<ProductoDTO> listarPorIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return productoRepository.findByIdIn(ids).stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> filtrarProductos(String categoria, Double precioMax, List<String> marcas) {
        List<String> marcasFiltradas = (marcas != null && marcas.isEmpty()) ? null : marcas;
        return productoRepository.filtrarProductos(categoria, precioMax, marcasFiltradas)
                .stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    // --- MÉTODOS PRIVADOS DE CONVERSIÓN ---

    private ProductoDTO convertirAProductoDTO(Producto producto) {
        String nombreCategoria = (producto.getCategoria() != null) ? producto.getCategoria().getNombre() : "Sin Categoría";
        int stock = (producto.getInventario() != null) ? producto.getInventario().getStockActual() : 0;

        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getOriginalPrice(),
                stock,
                producto.getImagenUrl(),
                nombreCategoria,
                producto.getDestacado(),
                producto.getRating()
        );
    }

    // El método "convertirACategoriaDTO" se ha eliminado porque su lógica se movió a "listarCategorias".
}