package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.service.AlmacenamientoService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import com.GoldenFeet.GoldenFeets.repository.InventarioMovimientoRepository;
import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoCreateDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoUpdateDTO;
import com.GoldenFeet.GoldenFeets.entity.Categoria;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository;
import com.GoldenFeet.GoldenFeets.repository.DetalleVentaRepository;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final InventarioMovimientoRepository inventarioMovimientoRepository;
    private final AlmacenamientoService almacenamientoService;


    // --- MÉTODOS DE LECTURA ---

    @Override
    public List<ProductoDTO> listarTodos() {
        // Asumiendo que Producto.id es Long
        List<Producto> productos = productoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return productos.stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoDTO> buscarPorId(Integer id) {
        // 💥 CORRECCIÓN: Convertimos el Integer de entrada a Long, ya que ProductoRepository espera Long.
        return productoRepository.findById(id.longValue())
                .map(this::convertirAProductoDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(this::convertirACategoriaDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarDestacados() {
        return productoRepository.findByDestacado(true).stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarPorCategoria(String nombreCategoria) {
        return productoRepository.findByCategoriaNombre(nombreCategoria).stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> filtrarProductos(String categoria, Double precioMax, List<String> marcas) {
        return productoRepository.findAll().stream()
                .filter(p -> categoria == null || p.getCategoria().getNombre().equalsIgnoreCase(categoria))
                .filter(p -> precioMax == null || p.getPrecio().doubleValue() <= precioMax)
                .filter(p -> marcas == null || marcas.isEmpty() || marcas.contains(p.getMarca()))
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listarMarcasDistintas() {
        return productoRepository.findMarcasDistintas();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarPorIds(List<Integer> ids) {
        // 💥 CORRECCIÓN: Convertimos List<Integer> a List<Long>
        List<Long> idLongs = ids.stream().map(Integer::longValue).collect(Collectors.toList());
        return productoRepository.findAllById(idLongs).stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> obtenerProductosRecientes(int cantidad) {
        Pageable limit = PageRequest.of(0, cantidad, Sort.by(Sort.Direction.DESC, "id"));
        return productoRepository.findAll(limit).getContent().stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    // --- MÉTODOS DE ESCRITURA ---

    @Override
    @Transactional
    public Producto crearProducto(ProductoCreateDTO productoDTO) {
        // Categoria.id es Integer
        Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + productoDTO.getCategoriaId()));

        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre(productoDTO.getNombre());
        nuevoProducto.setDescripcion(productoDTO.getDescripcion());
        nuevoProducto.setPrecio(productoDTO.getPrecio());
        nuevoProducto.setOriginalPrice(productoDTO.getOriginalPrice());

        nuevoProducto.setStock(0);

        nuevoProducto.setMarca(productoDTO.getMarca());
        nuevoProducto.setDestacado(productoDTO.isDestacado());
        nuevoProducto.setCategoria(categoria);

        if (productoDTO.getImagenArchivo() != null && !productoDTO.getImagenArchivo().isEmpty()) {
            String nombreArchivo = almacenamientoService.almacenarArchivo(productoDTO.getImagenArchivo());
            nuevoProducto.setImagenNombre(nombreArchivo);
        }

        Producto productoGuardado = productoRepository.save(nuevoProducto);

        // Registrar el movimiento inicial
        InventarioMovimiento movimientoInicial = new InventarioMovimiento();
        movimientoInicial.setProducto(productoGuardado);
        movimientoInicial.setTipoMovimiento("INGRESO_INICIAL");
        movimientoInicial.setCantidad(0);
        movimientoInicial.setMotivo("Creación de producto");
        movimientoInicial.setFecha(LocalDateTime.now());

        inventarioMovimientoRepository.save(movimientoInicial);

        return productoGuardado;
    }

    @Override
    @Transactional
    public Producto actualizarProducto(Integer id, ProductoUpdateDTO productoDTO) {
        // 💥 CORRECCIÓN: Convertimos el Integer de entrada a Long.
        Producto productoExistente = productoRepository.findById(id.longValue())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

        Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + productoDTO.getCategoriaId()));

        productoExistente.setNombre(productoDTO.getNombre());
        productoExistente.setDescripcion(productoDTO.getDescripcion());
        productoExistente.setPrecio(productoDTO.getPrecio());
        productoExistente.setOriginalPrice(productoDTO.getOriginalPrice());

        productoExistente.setMarca(productoDTO.getMarca());
        productoExistente.setDestacado(productoDTO.isDestacado());
        productoExistente.setCategoria(categoria);

        MultipartFile imagenArchivo = productoDTO.getImagenArchivo();
        if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
            almacenamientoService.eliminarArchivo(productoExistente.getImagenNombre());
            String nuevoNombreArchivo = almacenamientoService.almacenarArchivo(imagenArchivo);
            productoExistente.setImagenNombre(nuevoNombreArchivo);
        }

        Producto productoActualizado = productoRepository.save(productoExistente);

        return productoActualizado;
    }

    @Override
    @Transactional
    public void eliminarProducto(Integer id) {
        // 💥 CORRECCIÓN: Convertimos el Integer de entrada a Long.
        Long idLong = id.longValue();

        Producto producto = productoRepository.findById(idLong)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

        // Usamos el idLong para buscar movimientos y detalles
        List<InventarioMovimiento> movimientos = inventarioMovimientoRepository.findByProducto_Id(idLong);
        for (InventarioMovimiento movimiento : movimientos) {
            movimiento.setProducto(null);
        }
        inventarioMovimientoRepository.saveAll(movimientos);

        detalleVentaRepository.deleteByProducto_Id(idLong);
        almacenamientoService.eliminarArchivo(producto.getImagenNombre());
        productoRepository.deleteById(idLong);
    }

    // --- MÉTODOS PRIVADOS DE CONVERSIÓN ---

    private ProductoDTO convertirAProductoDTO(Producto producto) {
        // Producto.id es Long, pero DTO espera Integer
        Integer productoId = (producto.getId() != null) ? producto.getId().intValue() : null;

        // Categoria.idCategoria es Integer
        Integer categoriaId = (producto.getCategoria() != null)
                ? producto.getCategoria().getIdCategoria() : null;

        String nombreCategoria = (producto.getCategoria() != null)
                ? producto.getCategoria().getNombre() : "Sin Categoría";
        int stock = producto.getStock() != null ? producto.getStock() : 0;

        String imagenNombre = producto.getImagenNombre();
        String imagenUrlFinal = null;

        if (imagenNombre != null && !imagenNombre.isEmpty()) {
            if (imagenNombre.startsWith("http://") || imagenNombre.startsWith("https://")) {
                imagenUrlFinal = imagenNombre;
            } else {
                imagenUrlFinal = ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/api/imagenes/")
                        .path(imagenNombre)
                        .toUriString();
            }
        }

        return new ProductoDTO(
                productoId,
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getOriginalPrice(),
                stock,
                imagenUrlFinal,
                producto.getMarca(),
                categoriaId,
                nombreCategoria,
                producto.getDestacado(),
                producto.getRating()
        );
    }

    private CategoriaDTO convertirACategoriaDTO(Categoria categoria) {
        String imagenNombre = categoria.getImagenNombre();
        String imagenUrlFinal = null;

        if (imagenNombre != null && !imagenNombre.isEmpty()) {
            if (imagenNombre.startsWith("http://") || imagenNombre.startsWith("https://")) {
                imagenUrlFinal = imagenNombre;
            } else {
                imagenUrlFinal = ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/api/imagenes/")
                        .path(imagenNombre)
                        .toUriString();
            }
        }

        return new CategoriaDTO(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                imagenUrlFinal
        );
    }
}