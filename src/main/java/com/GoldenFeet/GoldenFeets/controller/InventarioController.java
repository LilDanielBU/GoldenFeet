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
import org.springframework.security.core.Authentication; // <--- IMPORTANTE
import org.springframework.security.core.authority.SimpleGrantedAuthority; // <--- IMPORTANTE
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/inventario")
// CAMBIO 1: Permitir que tanto el Gerente como el Admin usen este controlador
@PreAuthorize("hasAnyRole('GERENTEINVENTARIO', 'ADMIN')")
@RequiredArgsConstructor
public class InventarioController {

    private final ProductoService productoService;
    private final CategoriaRepository categoriaRepository;
    private final AlmacenamientoService almacenamientoService;
    private final InventarioMovimientoService inventarioMovimientoService;

    /**
     * Muestra la tabla principal de productos (Vista del Gerente de Inventario).
     */
    @GetMapping("/panel")
    public String mostrarPanelInventario(Model model) {
        List<ProductoDTO> listaProductos = productoService.listarTodos();
        model.addAttribute("productos", listaProductos);
        model.addAttribute("titulo", "Panel de Gestión de Inventario");

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
                                        RedirectAttributes redirectAttributes,
                                        Authentication authentication) { // <--- Inyectamos Authentication

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error de validación: La cantidad debe ser mayor o igual a 1 y el motivo es obligatorio.");
            return determinarRedireccion(authentication); // <--- Usamos redirección inteligente
        }

        try {
            inventarioMovimientoService.registrarIngreso(ingresoDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "¡Stock agregado! Se sumaron " + ingresoDTO.getCantidad() + " unidades.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return determinarRedireccion(authentication); // <--- Usamos redirección inteligente
    }

    // =============================================================
    // 2. REGISTRO DE SALIDA (RESTA) DE STOCK
    // =============================================================

    @PostMapping("/salida-stock")
    public String registrarSalidaStock(@Valid @ModelAttribute("ingresoDTO") IngresoDTO salidaDTO,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Authentication authentication) { // <--- Inyectamos Authentication

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error de validación: La cantidad debe ser >= 1 y el motivo es obligatorio.");
            return determinarRedireccion(authentication);
        }

        try {
            inventarioMovimientoService.registrarSalida(salidaDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "¡Éxito! Se retiraron " + salidaDTO.getCantidad() + " unidades.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return determinarRedireccion(authentication);
    }


    // =============================================================
    // MÉTODOS DE GESTIÓN DE PRODUCTOS (Crear, Editar, Eliminar)
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
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model,
                                  Authentication authentication) { // <--- Inyectamos Authentication
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("titulo", "Registrar Nuevo Producto");
            return "producto-form";
        }
        try {
            productoService.crearProducto(productoDTO);
            redirectAttributes.addFlashAttribute("successMessage", "¡Producto registrado exitosamente!");
            return determinarRedireccion(authentication); // <--- Redirección inteligente
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al registrar: " + e.getMessage());
            return "redirect:/inventario/productos/nuevo";
        }
    }

    @GetMapping("/productos/editar/{id}")
    public String mostrarFormularioDeEdicion(@PathVariable("id") Integer id,
                                             Model model,
                                             RedirectAttributes redirectAttributes,
                                             Authentication authentication) {
        Optional<ProductoDTO> productoOpt = productoService.buscarPorId(id);
        if (productoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado.");
            return determinarRedireccion(authentication);
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
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes,
                                        Model model,
                                        Authentication authentication) { // <--- Inyectamos Authentication

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
            return determinarRedireccion(authentication); // <--- Redirección inteligente
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar: " + e.getMessage());
            return "redirect:/inventario/productos/editar/" + id;
        }
    }

    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Integer id,
                                   RedirectAttributes redirectAttributes,
                                   Authentication authentication) { // <--- Inyectamos Authentication
        try {
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("successMessage", "Producto eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar: " + e.getMessage());
        }
        return determinarRedireccion(authentication); // <--- Redirección inteligente
    }

    // =============================================================
    // MÉTODO PRIVADO PARA LA REDIRECCIÓN INTELIGENTE
    // =============================================================
    private String determinarRedireccion(Authentication authentication) {
        // Si el usuario es ADMIN, lo devolvemos a SU panel general
        if (authentication != null &&
                authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/admin/panel";
        }
        // Si es GERENTE (u otro), lo devolvemos al panel de inventario normal
        return "redirect:/inventario/panel";
    }
}