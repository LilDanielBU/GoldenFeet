package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.entity.Categoria;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import lombok.RequiredArgsConstructor;
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

    @Override
    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(this::convertirACategoriaDTO)
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

    // --- MÉTODO DE CONVERSIÓN CORREGIDO ---

    private ProductoDTO convertirAProductoDTO(Producto producto) {
        String nombreCategoria = (producto.getCategoria() != null) ? producto.getCategoria().getNombre() : "Sin Categoría";

        // Obtenemos el stock desde la entidad Inventario asociada al producto
        // Se asigna 0 si no hay una entrada de inventario para evitar errores
        int stock = (producto.getInventario() != null) ? producto.getInventario().getStockActual() : 0;

        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getOriginalPrice(),
                stock, // <-- Se usa la variable de stock obtenida del inventario
                producto.getImagenUrl(),
                nombreCategoria,
                producto.getDestacado(), // Asumiendo que el campo es Boolean
                producto.getRating()     // Asumiendo que el campo es Float
        );
    }

    private CategoriaDTO convertirACategoriaDTO(Categoria categoria) {
        // Asumiendo que Categoria tiene estos getters. Ajusta si es necesario.
        return new CategoriaDTO(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getImagenUrl()
        );
    }
}