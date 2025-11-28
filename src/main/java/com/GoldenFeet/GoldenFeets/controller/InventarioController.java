package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.IngresoDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoCreateDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoUpdateDTO;
import com.GoldenFeet.GoldenFeets.entity.Categoria;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository;
import com.GoldenFeet.GoldenFeets.service.AlmacenamientoService;
import com.GoldenFeet.GoldenFeets.service.InventarioMovimientoService;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/inventario")
@PreAuthorize("hasRole('GERENTEINVENTARIO')")
@RequiredArgsConstructor
public class InventarioController {

    private final ProductoService productoService;
    private final CategoriaRepository categoriaRepository;
    private final AlmacenamientoService almacenamientoService;
    private final InventarioMovimientoService inventarioMovimientoService;

    /**
     * Muestra la tabla principal de productos y las tarjetas de estadísticas (KPIs).
     */
    @GetMapping("/panel")
    public String mostrarPanelInventario(Model model) {
        // 1. Obtener la lista de productos
        List<ProductoDTO> listaProductos = productoService.listarTodos();
        model.addAttribute("productos", listaProductos);
        model.addAttribute("titulo", "Panel de Gestión de Inventario");

        // --- CORRECCIÓN: CÁLCULOS PARA LAS TARJETAS ---

        // A. Total de Referencias (Productos únicos)
        model.addAttribute("totalProductos", listaProductos.size());

        // B. Productos Agotados (Stock igual a 0)
        long agotados = listaProductos.stream()
                .filter(p -> p.getStock() == 0)
                .count();
        model.addAttribute("productosAgotados", agotados);

        // C. Stock Bajo (Por ejemplo, menos de 5 unidades y mayor a 0)
        long stockBajo = listaProductos.stream()
                .filter(p -> p.getStock() > 0 && p.getStock() < 5)
                .count();
        model.addAttribute("productosStockBajo", stockBajo);

        // D. Valor Total del Inventario (Usando el servicio existente)
        double valorTotal = productoService.calcularValorTotalInventario();
        model.addAttribute("valorTotalInventario", valorTotal);

        // ----------------------------------------------

        // Inicializa el DTO para el formulario modal
        if (!model.containsAttribute("ingresoDTO")) {
            model.addAttribute("ingresoDTO", new IngresoDTO());
        }

        return "inventario-panel";
    }

    // =============================================================
    // 1. REGISTRO DE INGRESO (SUMA) DE STOCK
    // =============================================================

    @PostMapping("/ingreso-stock")
    public String registrarIngresoStock(@Valid @ModelAttribute("ingresoDTO") IngresoDTO ingresoDTO,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error de validación en ingreso: La cantidad debe ser mayor o igual a 1 y el motivo es obligatorio.");
            return "redirect:/inventario/panel";
        }

        try {
            inventarioMovimientoService.registrarIngreso(ingresoDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "¡Stock agregado exitosamente! Se sumaron " + ingresoDTO.getCantidad() +
                            " unidades al Producto ID " + ingresoDTO.getProductoId() + ".");

        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Producto no encontrado con el ID especificado.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error de negocio: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar el ingreso de stock: " + e.getMessage());
        }

        return "redirect:/inventario/panel";
    }

    // =============================================================
    // 2. REGISTRO DE SALIDA (RESTA) DE STOCK
    // =============================================================

    @PostMapping("/salida-stock")
    public String registrarSalidaStock(@Valid @ModelAttribute("ingresoDTO") IngresoDTO salidaDTO,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error de validación en salida: La cantidad debe ser >= 1 y el motivo es obligatorio.");
            return "redirect:/inventario/panel";
        }

        try {
            inventarioMovimientoService.registrarSalida(salidaDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "¡Éxito! Se han retirado " + salidaDTO.getCantidad() +
                            " unidades del producto ID " + salidaDTO.getProductoId() + ".");

        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Producto no encontrado.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al registrar la salida: " + e.getMessage());
        }

        return "redirect:/inventario/panel";
    }


    // =============================================================
    // MÉTODOS DE GESTIÓN DE PRODUCTOS
    // =============================================================

    @GetMapping("/productos/nuevo")
    public String mostrarFormularioDeProducto(Model model) {
        List<Categoria> categorias = categoriaRepository.findAll();
        model.addAttribute("productoDTO", new ProductoCreateDTO());
        model.addAttribute("categorias", categorias);
        model.addAttribute("titulo", "Registrar Nuevo Producto");
        return "producto-form";
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(@Valid @ModelAttribute("productoDTO") ProductoCreateDTO productoDTO,
                                  BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("titulo", "Registrar Nuevo Producto");
            return "producto-form";
        }
        try {
            productoService.crearProducto(productoDTO);
            redirectAttributes.addFlashAttribute("successMessage", "¡Producto registrado exitosamente!");
            return "redirect:/inventario/panel";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al registrar el producto: " + e.getMessage());
            return "redirect:/inventario/productos/nuevo";
        }
    }

    @GetMapping("/productos/editar/{id}")
    public String mostrarFormularioDeEdicion(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ProductoDTO> productoOpt = productoService.buscarPorId(id);
        if (productoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado.");
            return "redirect:/inventario/panel";
        }

        ProductoDTO productoDTO = productoOpt.get();
        ProductoUpdateDTO updateDTO = new ProductoUpdateDTO();

        updateDTO.setId(productoDTO.getId());
        updateDTO.setNombre(productoDTO.getNombre());
        updateDTO.setDescripcion(productoDTO.getDescripcion());
        updateDTO.setPrecio(productoDTO.getPrecio());
        updateDTO.setOriginalPrice(productoDTO.getOriginalPrice());
        updateDTO.setMarca(productoDTO.getMarca());
        updateDTO.setDestacado(productoDTO.getDestacado());
        updateDTO.setCategoriaId(productoDTO.getCategoriaId());

        model.addAttribute("productoDTO", updateDTO);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("isEditMode", true);
        model.addAttribute("imagenActualUrl", productoDTO.getImagenUrl());

        return "producto-form";
    }

    @PostMapping("/productos/actualizar/{id}")
    public String procesarActualizacion(@PathVariable("id") Integer id,
                                        @Valid @ModelAttribute("productoDTO") ProductoUpdateDTO productoDTO,
                                        BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("titulo", "Editar Producto");
            model.addAttribute("isEditMode", true);
            productoService.buscarPorId(id).ifPresent(p -> model.addAttribute("imagenActualUrl", p.getImagenUrl()));
            return "producto-form";
        }
        try {
            productoService.actualizarProducto(id, productoDTO);
            redirectAttributes.addFlashAttribute("successMessage", "¡Producto actualizado exitosamente!");
            return "redirect:/inventario/panel";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el producto: " + e.getMessage());
            return "redirect:/inventario/productos/editar/" + id;
        }
    }

    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("successMessage", "Producto eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el producto: " + e.getMessage());
        }
        return "redirect:/inventario/panel";
    }
}