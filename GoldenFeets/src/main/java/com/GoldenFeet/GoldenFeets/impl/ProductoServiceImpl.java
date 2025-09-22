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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    public List<ProductoDTO> obtenerProductosRecientes(int limit) {
        // Creamos un objeto Pageable para limitar los resultados a 'limit'
        Pageable pageable = PageRequest.of(0, limit);

        return productoRepository.findProductosRecientes(pageable).stream()
                .map(this::convertirAProductoDTO) // Asumo que tienes un método para convertir
                .collect(Collectors.toList());
    }

    private ProductoDTO convertirAProductoDTO(Producto producto) {
        // Verificamos si producto o su categoría son nulos para evitar errores
        if (producto == null) {
            return null;
        }
        String nombreCategoria = (producto.getCategoria() != null) ? producto.getCategoria().getNombre() : null;

        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getOriginalPrice(),
                producto.getStock(),
                producto.getImagenUrl(),
                nombreCategoria, // Obtenemos el nombre desde la entidad Categoria relacionada
                producto.isDestacado(),
                producto.getRating()
        );
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

    // --- NUEVOS MÉTODOS IMPLEMENTADOS ---

    @Override
    public List<ProductoDTO> buscarPorNombre(String nombre) {
        // Llama al método del repositorio y convierte los resultados
        return productoRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> listarPorCategoria(String nombreCategoria) {
        // Llama al método del repositorio y convierte los resultados
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
            return List.of(); // Devuelve una lista vacía si no hay IDs
        }
        return productoRepository.findByIdIn(ids).stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> filtrarProductos(String categoria, Double precioMax, List<String> marcas) {
        // Si la lista de marcas está vacía, la tratamos como nula para que la consulta funcione
        List<String> marcasFiltradas = (marcas != null && marcas.isEmpty()) ? null : marcas;

        return productoRepository.filtrarProductos(categoria, precioMax, marcasFiltradas)
                .stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }


    private CategoriaDTO convertirACategoriaDTO(Categoria categoria) {
        return new CategoriaDTO(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getImagenUrl()
        );
    }
}