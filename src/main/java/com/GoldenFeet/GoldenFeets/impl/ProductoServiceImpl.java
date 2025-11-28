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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
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


    // --- IMPLEMENTACIÓN DE MÉTODOS REQUERIDOS POR LA INTERFAZ ---

    @Transactional(readOnly = true)
    @Override
    public Collection<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public double calcularValorTotalInventario() {
        return productoRepository.findAll().stream()
                .mapToDouble(p -> {
                    // Validamos nulos para evitar errores
                    double precio = p.getPrecio() != null ? p.getPrecio() : 0.0;
                    int stock = p.getStock() != null ? p.getStock() : 0;
                    return precio * stock;
                })
                .sum();
    }

    // --- MÉTODOS DE LECTURA EXISTENTES ---

    @Override
    public List<ProductoDTO> listarTodos() {
        List<Producto> productos = productoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return productos.stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoDTO> buscarPorId(Integer id) {
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
                .filter(p -> precioMax == null || p.getPrecio() <= precioMax)
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
        Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId().longValue())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + productoDTO.getCategoriaId()));

        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre(productoDTO.getNombre());
        nuevoProducto.setDescripcion(productoDTO.getDescripcion());

        // Conversión segura de BigDecimal a Double
        if (productoDTO.getPrecio() != null) {
            nuevoProducto.setPrecio(productoDTO.getPrecio().doubleValue());
        }
        if (productoDTO.getOriginalPrice() != null) {
            nuevoProducto.setOriginalPrice(productoDTO.getOriginalPrice().doubleValue());
        }

        // Stock inicial siempre es 0 hasta que se haga un ingreso de inventario
        nuevoProducto.setStock(0);
        nuevoProducto.setMarca(productoDTO.getMarca());

        // En crearProducto usamos isDestacado porque CreateDTO suele usar boolean primitivo
        nuevoProducto.setDestacado(productoDTO.isDestacado());

        nuevoProducto.setCategoria(categoria);

        // Guardar Imagen si existe
        if (productoDTO.getImagenArchivo() != null && !productoDTO.getImagenArchivo().isEmpty()) {
            String nombreArchivo = almacenamientoService.almacenarArchivo(productoDTO.getImagenArchivo());
            nuevoProducto.setImagenNombre(nombreArchivo);
        }

        Producto productoGuardado = productoRepository.save(nuevoProducto);

        // Registrar movimiento inicial en el Kardex/Historial
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
    @Transactional // IMPORTANTE: Asegura que la actualización se cometa en la BD
    public Producto actualizarProducto(Integer id, ProductoUpdateDTO productoDTO) {
        // 1. Buscamos el producto existente
        Producto productoExistente = productoRepository.findById(id.longValue())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

        // 2. Buscamos la categoría
        Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId().longValue())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + productoDTO.getCategoriaId()));

        // 3. Actualizamos los campos
        productoExistente.setNombre(productoDTO.getNombre());
        productoExistente.setDescripcion(productoDTO.getDescripcion());
        productoExistente.setMarca(productoDTO.getMarca());

        // --- CORRECCIÓN AQUÍ ---
        // Cambiamos isDestacado() por getDestacado() porque ahora es Boolean (Wrapper) en el DTO
        productoExistente.setDestacado(productoDTO.getDestacado());

        productoExistente.setCategoria(categoria);

        // Conversión segura de BigDecimal a Double para el precio
        if (productoDTO.getPrecio() != null) {
            productoExistente.setPrecio(productoDTO.getPrecio().doubleValue());
        }
        if (productoDTO.getOriginalPrice() != null) {
            productoExistente.setOriginalPrice(productoDTO.getOriginalPrice().doubleValue());
        }

        // 4. Actualización de Imagen (Si se subió una nueva)
        MultipartFile imagenArchivo = productoDTO.getImagenArchivo();
        if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
            // Eliminar imagen anterior para no llenar el servidor de basura
            almacenamientoService.eliminarArchivo(productoExistente.getImagenNombre());
            // Guardar nueva
            String nuevoNombreArchivo = almacenamientoService.almacenarArchivo(imagenArchivo);
            productoExistente.setImagenNombre(nuevoNombreArchivo);
        }

        // 5. Guardamos
        return productoRepository.save(productoExistente);
    }

    @Override
    @Transactional
    public void eliminarProducto(Integer id) {
        // Mantenemos la conversión a Long solo para los repositorios que lo piden así
        Long idLong = id.longValue();

        // 1. Buscar producto (ProductoRepository parece usar Long)
        Producto producto = productoRepository.findById(idLong)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

        // 2. Desvincular movimientos (InventarioMovimientoRepository parece usar Long)
        List<InventarioMovimiento> movimientos = inventarioMovimientoRepository.findByProducto_Id(idLong);
        for (InventarioMovimiento movimiento : movimientos) {
            movimiento.setProducto(null);
        }
        inventarioMovimientoRepository.saveAll(movimientos);

        // 3. Eliminar detalles de venta (CORRECCIÓN AQUÍ)
        // Usamos 'id' (Integer) directamente, no 'idLong'
        detalleVentaRepository.deleteByProducto_Id(id);

        // 4. Eliminar archivo de imagen
        almacenamientoService.eliminarArchivo(producto.getImagenNombre());

        // 5. Eliminar producto (ProductoRepository parece usar Long)
        productoRepository.deleteById(idLong);
    }

    // --- MÉTODOS PRIVADOS DE CONVERSIÓN ---

    private ProductoDTO convertirAProductoDTO(Producto producto) {
        Integer productoId = (producto.getId() != null) ? producto.getId().intValue() : null;
        Integer categoriaId = (producto.getCategoria() != null) ? producto.getCategoria().getIdCategoria() : null;
        String nombreCategoria = (producto.getCategoria() != null) ? producto.getCategoria().getNombre() : "Sin Categoría";
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

        BigDecimal precioBD = producto.getPrecio() != null ? BigDecimal.valueOf(producto.getPrecio()) : BigDecimal.ZERO;
        BigDecimal originalPriceBD = producto.getOriginalPrice() != null ? BigDecimal.valueOf(producto.getOriginalPrice()) : BigDecimal.ZERO;
        Float ratingFloat = producto.getRating() != null ? producto.getRating().floatValue() : 0.0f;

        return new ProductoDTO(
                productoId,
                producto.getNombre(),
                producto.getDescripcion(),
                precioBD,
                originalPriceBD,
                stock,
                imagenUrlFinal,
                producto.getMarca(),
                categoriaId,
                nombreCategoria,
                producto.getDestacado(),
                ratingFloat
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