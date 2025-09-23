package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoCreateDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoUpdateDTO;
import com.GoldenFeet.GoldenFeets.entity.Categoria;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    // ... (todos tus otros métodos públicos no necesitan cambios)
    @Override
    public List<ProductoDTO> listarTodos() {
        return productoRepository.findAll().stream().map(this::convertirAProductoDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<ProductoDTO> buscarPorId(Integer id) {
        return productoRepository.findById(Long.valueOf(id)).map(this::convertirAProductoDTO);
    }

    @Override
    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(categoria -> new CategoriaDTO(
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
    public List<ProductoDTO> listarPorIds(List<Integer> ids) {
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
        return productoRepository.filtrarProductos(categoria, precioMax, marcasFiltradas).stream().map(this::convertirAProductoDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Producto crearProducto(ProductoCreateDTO productoDTO) {
        Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + productoDTO.getCategoriaId()));

        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre(productoDTO.getNombre());
        nuevoProducto.setDescripcion(productoDTO.getDescripcion());
        nuevoProducto.setPrecio(productoDTO.getPrecio());
        nuevoProducto.setOriginalPrice(productoDTO.getOriginalPrice());
        nuevoProducto.setStock(productoDTO.getStock());
        nuevoProducto.setImagenUrl(productoDTO.getImagenUrl());
        nuevoProducto.setMarca(productoDTO.getMarca());
        nuevoProducto.setRating(productoDTO.getRating() != null ? productoDTO.getRating() : 0.0f);
        nuevoProducto.setDestacado(productoDTO.isDestacado());
        nuevoProducto.setCategoria(categoria);
        return productoRepository.save(nuevoProducto);
    }

    @Override
    @Transactional
    public Producto actualizarProducto(Integer id, ProductoUpdateDTO productoDTO) {
        Producto productoExistente = productoRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

        Categoria categoria = categoriaRepository.findById(Long.valueOf(productoDTO.getCategoriaId()))
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + productoDTO.getCategoriaId()));

        productoExistente.setNombre(productoDTO.getNombre());
        productoExistente.setDescripcion(productoDTO.getDescripcion());
        productoExistente.setPrecio(productoDTO.getPrecio());
        productoExistente.setOriginalPrice(productoDTO.getOriginalPrice());
        productoExistente.setStock(productoDTO.getStock());
        productoExistente.setImagenUrl(productoDTO.getImagenUrl());
        productoExistente.setMarca(productoDTO.getMarca());
        productoExistente.setRating(productoDTO.getRating());
        productoExistente.setDestacado(productoDTO.isDestacado());
        productoExistente.setCategoria(categoria);

        return productoRepository.save(productoExistente);
    }

    @Override
    public void eliminarProducto(Integer id) {
        Long productoId = Long.valueOf(id);
        if (!productoRepository.existsById(productoId)) {
            throw new EntityNotFoundException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(productoId);
    }

    // --- MÉTODO PRIVADO CORREGIDO ---
    private ProductoDTO convertirAProductoDTO(Producto producto) {
        // CORRECCIÓN: La variable ahora es Integer para coincidir con la entidad y el DTO
        Integer categoriaId = (producto.getCategoria() != null) ? producto.getCategoria().getIdCategoria() : null;
        String nombreCategoria = (producto.getCategoria() != null) ? producto.getCategoria().getNombre() : "Sin Categoría";
        int stock = producto.getStock() != null ? producto.getStock() : 0;

        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getOriginalPrice(),
                stock,
                producto.getImagenUrl(),
                producto.getMarca(),
                categoriaId, // Ahora los tipos coinciden
                nombreCategoria,
                producto.getDestacado(),
                producto.getRating()
        );
    }
}