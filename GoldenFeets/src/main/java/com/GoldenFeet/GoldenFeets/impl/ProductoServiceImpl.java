package com.GoldenFeet.GoldenFeets.impl;

// --- INICIO DE MODIFICACIONES (IMPORTS) ---
import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import com.GoldenFeet.GoldenFeets.repository.InventarioMovimientoRepository;
import java.time.LocalDateTime; // <-- Asegúrate que sea LocalDateTime
// --- FIN DE MODIFICACIONES (IMPORTS) ---

import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoCreateDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoUpdateDTO;
import com.GoldenFeet.GoldenFeets.entity.Categoria;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository;
import com.GoldenFeet.GoldenFeets.repository.DetalleVentaRepository;
// import com.GoldenFeet.GoldenFeets.repository.InventarioRepository; // <-- 1. ELIMINADO
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
    private final DetalleVentaRepository detalleVentaRepository;
    // private final InventarioRepository inventarioRepository; // <-- 1. ELIMINADO
    private final InventarioMovimientoRepository inventarioMovimientoRepository;


    // --- MÉTODOS DE LECTURA (CORRECTOS) ---

    @Override
    public List<ProductoDTO> listarTodos() {
        List<Producto> productos = productoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return productos.stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductoDTO> buscarPorId(Integer id) {
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
        return productoRepository.findByCategoriaNombre(nombreCategoria).stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> filtrarProductos(String categoria, Double precioMax, List<String> marcas) {
        return productoRepository.findAll().stream()
                .filter(p -> categoria == null || p.getCategoria().getNombre().equalsIgnoreCase(categoria))
                .filter(p -> precioMax == null || p.getPrecio().doubleValue() <= precioMax)
                .filter(p -> marcas == null || marcas.isEmpty() || marcas.contains(p.getMarca()))
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listarMarcasDistintas() {
        return productoRepository.findMarcasDistintas();
    }

    @Override
    public List<ProductoDTO> listarPorIds(List<Integer> ids) {
        return productoRepository.findAllById(ids).stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> obtenerProductosRecientes(int cantidad) {
        Pageable limit = PageRequest.of(0, cantidad, Sort.by(Sort.Direction.DESC, "id"));
        return productoRepository.findAll(limit).getContent().stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    // --- MÉTODOS DE ESCRITURA (CORRECTOS) ---

    @Override
    @Transactional
    public Producto crearProducto(ProductoCreateDTO productoDTO) {
        // Correcto: usa Long.valueOf() para el ID de Categoría
        Categoria categoria = categoriaRepository.findById(Long.valueOf(productoDTO.getCategoriaId()))
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + productoDTO.getCategoriaId()));

        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre(productoDTO.getNombre());
        nuevoProducto.setDescripcion(productoDTO.getDescripcion());
        nuevoProducto.setPrecio(productoDTO.getPrecio());
        nuevoProducto.setOriginalPrice(productoDTO.getOriginalPrice());
        nuevoProducto.setStock(productoDTO.getStock()); // Correcto: usa el stock de la entidad Producto
        nuevoProducto.setImagenUrl(productoDTO.getImagenUrl());
        nuevoProducto.setMarca(productoDTO.getMarca());
        nuevoProducto.setRating(productoDTO.getRating() != null ? productoDTO.getRating() : 0.0f);
        nuevoProducto.setDestacado(productoDTO.isDestacado());
        nuevoProducto.setCategoria(categoria);

        Producto productoGuardado = productoRepository.save(nuevoProducto);

        // Correcto: registra el movimiento inicial
        if (productoGuardado.getStock() != null && productoGuardado.getStock() > 0) {
            InventarioMovimiento movimientoInicial = new InventarioMovimiento(
                    productoGuardado,
                    "INGRESO_INICIAL",
                    productoGuardado.getStock(),
                    "Creación de producto"
            );
            inventarioMovimientoRepository.save(movimientoInicial);
        }
        return productoGuardado;
    }

    @Override
    @Transactional
    public Producto actualizarProducto(Integer id, ProductoUpdateDTO productoDTO) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

        // Correcto: usa Long.valueOf() para el ID de Categoría
        Categoria categoria = categoriaRepository.findById(Long.valueOf(productoDTO.getCategoriaId()))
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + productoDTO.getCategoriaId()));

        int stockAntiguo = productoExistente.getStock() != null ? productoExistente.getStock() : 0;
        int stockNuevo = productoDTO.getStock();

        productoExistente.setNombre(productoDTO.getNombre());
        productoExistente.setDescripcion(productoDTO.getDescripcion());
        productoExistente.setPrecio(productoDTO.getPrecio());
        productoExistente.setOriginalPrice(productoDTO.getOriginalPrice());
        productoExistente.setStock(stockNuevo); // Correcto: usa el stock de la entidad Producto
        productoExistente.setImagenUrl(productoDTO.getImagenUrl());
        productoExistente.setMarca(productoDTO.getMarca());
        productoExistente.setRating(productoDTO.getRating());
        productoExistente.setDestacado(productoDTO.isDestacado());
        productoExistente.setCategoria(categoria);

        Producto productoActualizado = productoRepository.save(productoExistente);

        // Correcto: registra el ajuste de stock
        if (stockAntiguo != stockNuevo) {
            int diferencia = stockNuevo - stockAntiguo;
            String tipo = (diferencia > 0) ? "AJUSTE_INGRESO" : "AJUSTE_SALIDA";
            String motivo = "Ajuste manual (admin)";

            InventarioMovimiento movimientoAjuste = new InventarioMovimiento(
                    productoActualizado,
                    tipo,
                    diferencia,
                    motivo
            );
            inventarioMovimientoRepository.save(movimientoAjuste);
        }
        return productoActualizado;
    }

    @Override
    @Transactional
    public void eliminarProducto(Integer id) {
        if (!productoRepository.existsById(id)) {
            throw new EntityNotFoundException("Producto no encontrado con ID: " + id);
        }
        // Borramos de las tablas que tienen claves foráneas
        detalleVentaRepository.deleteByProducto_Id(id);
        // inventarioRepository.deleteByProducto_Id(id); // <-- 2. ELIMINADO (Ya no existe la entidad Inventario)

        // NO borramos 'inventario_movimientos' para mantener el historial

        productoRepository.deleteById(id);
    }

    // --- MÉTODOS PRIVADOS DE CONVERSIÓN (CORRECTOS) ---

    private ProductoDTO convertirAProductoDTO(Producto producto) {
        // Correcto: usa .intValue() para el DTO
        Integer categoriaId = (producto.getCategoria() != null)
                ? producto.getCategoria().getIdCategoria().intValue()
                : null;

        String nombreCategoria = (producto.getCategoria() != null)
                ? producto.getCategoria().getNombre()
                : "Sin Categoría";

        int stock = producto.getStock() != null ? producto.getStock() : 0; // Correcto: obtiene el stock del producto

        return new ProductoDTO(
                producto.getId(), // Correcto: es Integer
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getOriginalPrice(),
                stock,
                producto.getImagenUrl(),
                producto.getMarca(),
                categoriaId, // Correcto: es Integer
                nombreCategoria,
                producto.getDestacado(),
                producto.getRating()
        );
    }

    private CategoriaDTO convertirACategoriaDTO(Categoria categoria) {
        return new CategoriaDTO(
                categoria.getIdCategoria().intValue(), // Correcto: usa .intValue() para el DTO
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getImagenUrl()
        );
    }
}