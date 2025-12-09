package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoCreateDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoUpdateDTO;
import com.GoldenFeet.GoldenFeets.dto.VarianteDTO;
import com.GoldenFeet.GoldenFeets.entity.Categoria;
import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.entity.VarianteProducto;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository;
import com.GoldenFeet.GoldenFeets.repository.DetalleVentaRepository;
import com.GoldenFeet.GoldenFeets.repository.InventarioMovimientoRepository;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.repository.VarianteProductoRepository;
import com.GoldenFeet.GoldenFeets.service.AlmacenamientoService;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DetalleVentaRepository detalleVentaRepository; // Necesario para eliminar FK de Ventas
    private final InventarioMovimientoRepository inventarioMovimientoRepository;
    private final VarianteProductoRepository varianteRepository;
    private final AlmacenamientoService almacenamientoService;

    // ------------------------
    // MÉTODOS DE LECTURA
    // ------------------------

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
                    final double precio = (p.getPrecio() != null ? p.getPrecio() : 0.0);
                    // Sumar stock de todas las variantes
                    return p.getVariantes()
                            .stream()
                            .mapToDouble(v -> precio * (v.getStock() != null ? v.getStock() : 0))
                            .sum();
                })
                .sum();
    }

    @Override
    public List<ProductoDTO> listarTodos() {
        List<Producto> productos = productoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return productos.stream().map(this::convertirAProductoDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoDTO> buscarPorId(Long id) {
        return productoRepository.findById(id).map(this::convertirAProductoDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO obtenerProductoConVariantes(Long id) {
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto == null) {
            return null;
        }
        return convertirAProductoDTO(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirACategoriaDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarDestacados() {
        return productoRepository.findByDestacado(true)
                .stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarPorCategoria(String nombreCategoria) {
        return productoRepository.findByCategoriaNombre(nombreCategoria)
                .stream()
                .map(this::convertirAProductoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> filtrarProductos(String categoria, Double precioMax, List<String> marcas) {
        return productoRepository.findAll().stream()
                .filter(p -> categoria == null || (p.getCategoria() != null && p.getCategoria().getNombre().equalsIgnoreCase(categoria)))
                .map(this::convertirAProductoDTO)
                .filter(p -> precioMax == null || p.getPrecio().doubleValue() <= precioMax)
                .filter(p -> marcas == null || marcas.isEmpty() || marcas.contains(p.getMarca()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listarMarcasDistintas() {
        return productoRepository.findMarcasDistintas();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarPorIds(List<Long> ids) {
        return productoRepository.findAllById(ids).stream()
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

    // ---------------------------------------------------
    //         MÉTODO crearProducto
    // ---------------------------------------------------

    @Override
    @Transactional
    public Producto crearProducto(ProductoCreateDTO productoDTO) {

        Long catId = (productoDTO.getCategoriaId() != null) ? productoDTO.getCategoriaId().longValue() : null;

        Categoria categoria = categoriaRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre(productoDTO.getNombre());
        nuevoProducto.setDescripcion(productoDTO.getDescripcion());
        nuevoProducto.setMarca(productoDTO.getMarca());
        nuevoProducto.setDestacado(productoDTO.isDestacado());
        nuevoProducto.setCategoria(categoria);

        if (productoDTO.getPrecio() != null) nuevoProducto.setPrecio(productoDTO.getPrecio().doubleValue());
        if (productoDTO.getOriginalPrice() != null) nuevoProducto.setOriginalPrice(productoDTO.getOriginalPrice().doubleValue());

        if (productoDTO.getImagenArchivo() != null && !productoDTO.getImagenArchivo().isEmpty()) {
            String nombreArchivo = almacenamientoService.almacenarArchivo(productoDTO.getImagenArchivo());
            nuevoProducto.setImagenNombre(nombreArchivo);
        }

        Producto productoGuardado = productoRepository.save(nuevoProducto);

        if (productoDTO.getVariantes() != null) {
            for (VarianteDTO vDto : productoDTO.getVariantes()) {

                VarianteProducto variante = new VarianteProducto();
                variante.setProducto(productoGuardado);
                variante.setTalla(vDto.getTalla());
                variante.setColor(vDto.getColor());
                variante.setStock(vDto.getStock() != null ? vDto.getStock() : 0);

                MultipartFile imagenVariante = vDto.getImagenArchivo();

                if (imagenVariante != null && !imagenVariante.isEmpty()) {
                    String nombreArchivo = almacenamientoService.almacenarArchivo(imagenVariante);
                    variante.setImagenNombre(nombreArchivo);
                } else {
                    variante.setImagenNombre(productoGuardado.getImagenNombre());
                }

                VarianteProducto varianteGuardada = varianteRepository.save(variante);

                if (varianteGuardada.getStock() > 0) {
                    InventarioMovimiento mov = new InventarioMovimiento(
                            varianteGuardada,
                            "INGRESO_INICIAL",
                            varianteGuardada.getStock(),
                            "Creación producto"
                    );
                    inventarioMovimientoRepository.save(mov);
                }
            }
        }

        return productoGuardado;
    }

    // ---------------------------------------------------
    //                MÉTODO actualizarProducto
    // ---------------------------------------------------

    @Override
    @Transactional
    public Producto actualizarProducto(Long id, ProductoUpdateDTO productoDTO) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        producto.setNombre(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecio(productoDTO.getPrecio().doubleValue());
        producto.setOriginalPrice(productoDTO.getOriginalPrice() != null ? productoDTO.getOriginalPrice().doubleValue() : null);
        producto.setMarca(productoDTO.getMarca());
        producto.setDestacado(productoDTO.getDestacado());

        Long catId = (productoDTO.getCategoriaId() != null) ? productoDTO.getCategoriaId().longValue() : null;
        Categoria categoria = categoriaRepository.findById(catId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        producto.setCategoria(categoria);

        if (productoDTO.getImagenArchivo() != null && !productoDTO.getImagenArchivo().isEmpty()) {
            String nombreImagen = almacenamientoService.almacenarArchivo(productoDTO.getImagenArchivo());
            producto.setImagenNombre(nombreImagen);
        }

        // Lista temporal para recolectar todas las variantes que deben persistir (existentes y nuevas)
        List<VarianteProducto> variantesPersistentes = new ArrayList<>();


        if (productoDTO.getVariantes() != null) {

            for (VarianteDTO vDTO : productoDTO.getVariantes()) {
                if (vDTO.getId() != null) {
                    // --------------------------------------
                    // VARIANTE EXISTENTE (Actualización)
                    // --------------------------------------

                    VarianteProducto varianteExistente = varianteRepository.findById(vDTO.getId())
                            .orElse(null);

                    if (varianteExistente != null) {
                        varianteExistente.setTalla(vDTO.getTalla());
                        varianteExistente.setColor(vDTO.getColor());
                        // NOTA: El stock NO se actualiza aquí, solo con movimientos de inventario

                        // La lógica para la imagen es correcta: solo actualiza si se sube un nuevo archivo.
                        if (vDTO.getImagenArchivo() != null && !vDTO.getImagenArchivo().isEmpty()) {
                            String imgVar = almacenamientoService.almacenarArchivo(vDTO.getImagenArchivo());
                            varianteExistente.setImagenNombre(imgVar);
                        }

                        // Guardar la existente (esto es importante para las variantes existentes)
                        VarianteProducto varianteActualizada = varianteRepository.save(varianteExistente);

                        // CRÍTICO: Agregar la variante actualizada a la lista persistente
                        variantesPersistentes.add(varianteActualizada);
                    }
                } else {
                    // --------------------------------------
                    // VARIANTE NUEVA (Creación)
                    // --------------------------------------

                    VarianteProducto nueva = new VarianteProducto();
                    nueva.setTalla(vDTO.getTalla());
                    nueva.setColor(vDTO.getColor());
                    nueva.setStock(vDTO.getStock() != null ? vDTO.getStock() : 0);

                    // CRÍTICO: Asociación con el producto padre
                    nueva.setProducto(producto);

                    MultipartFile imagenVariante = vDTO.getImagenArchivo();

                    if (imagenVariante != null && !imagenVariante.isEmpty()) {
                        String imgVar = almacenamientoService.almacenarArchivo(imagenVariante);
                        nueva.setImagenNombre(imgVar);
                    } else {
                        // Si no se subió imagen para la nueva variante, usa la imagen principal del producto
                        nueva.setImagenNombre(producto.getImagenNombre());
                    }

                    VarianteProducto varianteGuardada = varianteRepository.save(nueva);

                    // CRÍTICO: Agregar la nueva variante guardada a la lista persistente
                    variantesPersistentes.add(varianteGuardada);

                    if (varianteGuardada.getStock() > 0) {
                        InventarioMovimiento mov = new InventarioMovimiento(
                                varianteGuardada,
                                "INGRESO_INICIAL",
                                varianteGuardada.getStock(),
                                "Creación de variante durante edición"
                        );
                        inventarioMovimientoRepository.save(mov);
                    }
                }
            }
        }

        // ************************************************************
        // CORRECCIÓN FINAL CRÍTICA 1: Sincronizar la colección del padre
        // ************************************************************
        // Esto le indica a Hibernate cuál es el nuevo estado de la colección de variantes
        // y es esencial para que la transacción funcione, especialmente con @OneToMany.
        producto.setVariantes(variantesPersistentes);


        // Guardar el producto principal (asegura que los cambios de precio/nombre/marca se persistan)
        // y que la colección de variantes se sincronice.
        Producto productoActualizado = productoRepository.save(producto);

        // ********************************************
        // CORRECCIÓN FINAL CRÍTICA 2: Forzar la ejecución del UPDATE
        // ********************************************
        productoRepository.flush();

        return productoActualizado;
    }

    // ---------------------------------------------------
    //        MÉTODO eliminarProducto
    // ---------------------------------------------------

    @Override
    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

        // 1. Clonar la lista de variantes (para evitar ConcurrentModificationException si Hibernate la manipula)
        List<VarianteProducto> variantesAEliminar = producto.getVariantes().stream().collect(Collectors.toList());

        // 2. Eliminar cada variante individualmente (maneja todas las FK y potencialmente el producto padre)
        for (VarianteProducto variante : variantesAEliminar) {
            // Lógica robusta de eliminación de variante (que maneja las FK)
            eliminarVariante(variante.getId());
        }

        // 3. (Opcional) Intentamos borrar la imagen física si el producto no se eliminó
        if (productoRepository.existsById(id)) { // Revisa si el producto padre aún existe
            if (producto.getImagenNombre() != null) {
                try {
                    almacenamientoService.eliminarArchivo(producto.getImagenNombre());
                } catch (Exception e) {
                    System.err.println("No se pudo eliminar la imagen del producto padre: " + e.getMessage());
                }
            }

            // 4. Eliminar el producto
            productoRepository.delete(producto);
        }
    }

    // ---------------------------------------------------
    //     MÉTODO eliminarVariante (CORRECCIÓN CRÍTICA)
    // ---------------------------------------------------
    /**
     * Elimina una variante de producto, asegurando la integridad referencial.
     */
    @Override
    @Transactional
    public void eliminarVariante(Long id) {
        VarianteProducto variante = varianteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Variante de producto no encontrada con ID: " + id));

        // 1. ELIMINAR DEPENDENCIAS FK (Detalles de Venta y Movimientos de Inventario)
        detalleVentaRepository.deleteByVarianteId(id);
        inventarioMovimientoRepository.deleteByVarianteId(id);

        // 2. ELIMINAR IMAGEN
        if (variante.getImagenNombre() != null && !variante.getImagenNombre().equals(variante.getProducto().getImagenNombre())) {
            try {
                almacenamientoService.eliminarArchivo(variante.getImagenNombre());
            } catch (Exception e) {
                System.err.println("No se pudo eliminar la imagen de la variante: " + e.getMessage());
            }
        }

        // 3. ELIMINAR LA VARIANTE
        varianteRepository.delete(variante);

        // 4. Lógica para verificar si el Producto padre queda sin variantes
        Long productoId = variante.getProducto().getId();
        // Contamos cuántas variantes quedan para este producto (debe ser 0)
        if (varianteRepository.countByProductoId(productoId) == 0) {

            Producto productoPadre = productoRepository.findById(productoId).orElse(null);

            if(productoPadre != null) {
                // Eliminamos la imagen del padre
                if (productoPadre.getImagenNombre() != null) {
                    try {
                        almacenamientoService.eliminarArchivo(productoPadre.getImagenNombre());
                    } catch (Exception e) {
                        System.err.println("No se pudo eliminar la imagen del producto padre (desde variante): " + e.getMessage());
                    }
                }
                // Eliminamos el registro final del producto.
                productoRepository.delete(productoPadre);
            }
        }
    }


    // ---------------------------------------------------
    //                   CONVERTIR A DTO
    // ---------------------------------------------------

    private ProductoDTO convertirAProductoDTO(Producto producto) {

        Integer productoId = (producto.getId() != null) ? producto.getId().intValue() : null;
        Integer categoriaId = (producto.getCategoria() != null && producto.getCategoria().getIdCategoria() != null)
                ? producto.getCategoria().getIdCategoria().intValue()
                : null;
        String nombreCategoria = (producto.getCategoria() != null) ? producto.getCategoria().getNombre() : "Sin Categoría";

        String imagenNombre = producto.getImagenNombre();
        // Nota: Asumimos que la URL real se construye en el front o con un endpoint aparte
        String imagenUrlFinal = imagenNombre;

        BigDecimal precioBD = producto.getPrecio() != null ? BigDecimal.valueOf(producto.getPrecio()) : BigDecimal.ZERO;
        BigDecimal originalPriceBD = producto.getOriginalPrice() != null ? BigDecimal.valueOf(producto.getOriginalPrice()) : BigDecimal.ZERO;
        Float ratingFloat = producto.getRating() != null ? producto.getRating().floatValue() : 0f;

        List<VarianteDTO> variantesDTO = null;
        if (producto.getVariantes() != null) {
            variantesDTO = producto.getVariantes()
                    .stream()
                    .map(v -> {
                        VarianteDTO vDto = new VarianteDTO();
                        vDto.setId(v.getId());
                        vDto.setTalla(v.getTalla());
                        vDto.setColor(v.getColor());
                        vDto.setStock(v.getStock());

                        String imgVar = v.getImagenNombre();
                        vDto.setImagenUrl(imgVar);

                        return vDto;
                    })
                    .collect(Collectors.toList());
        } else {
            variantesDTO = List.of();
        }

        int stockTotal = variantesDTO.stream().mapToInt(v -> (v.getStock() != null ? v.getStock() : 0)).sum();

        return new ProductoDTO(
                productoId,
                producto.getNombre(),
                producto.getDescripcion(),
                precioBD,
                originalPriceBD,
                stockTotal,
                imagenUrlFinal,
                producto.getMarca(),
                categoriaId,
                nombreCategoria,
                producto.getDestacado(),
                ratingFloat,
                variantesDTO
        );
    }

    private CategoriaDTO convertirACategoriaDTO(Categoria categoria) {

        String imagenNombre = categoria.getImagenNombre();
        String imagenUrlFinal = imagenNombre;

        Integer catId = (categoria.getIdCategoria() != null) ? categoria.getIdCategoria().intValue() : null;

        return new CategoriaDTO(
                catId,
                categoria.getNombre(),
                categoria.getDescripcion(),
                imagenUrlFinal
        );
    }
}