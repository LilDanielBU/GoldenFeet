package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.ProductoCreateDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoUpdateDTO;
import com.GoldenFeet.GoldenFeets.entity.Categoria;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
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

    // ... (otros métodos sin cambios)
    @GetMapping("/panel")
    public String mostrarPanelInventario(Model model) {
        List<ProductoDTO> listaProductos = productoService.listarTodos();
        model.addAttribute("productos", listaProductos);
        model.addAttribute("titulo", "Panel de Gestión de Inventario");
        return "inventario-panel";
    }

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
        updateDTO.setStock(productoDTO.getStock());
        updateDTO.setImagenUrl(productoDTO.getImagenUrl());
        updateDTO.setMarca(productoDTO.getMarca());
        updateDTO.setRating(productoDTO.getRating());
        updateDTO.setDestacado(productoDTO.getDestacado());
        updateDTO.setCategoriaId(productoDTO.getCategoriaId());

        model.addAttribute("productoDTO", updateDTO);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("isEditMode", true);
        return "producto-form";
    }

    // --- MÉTODO CON DEPURACIÓN AÑADIDA ---
    @PostMapping("/productos/actualizar/{id}")
    public String procesarActualizacion(@PathVariable("id") Integer id,
                                        @Valid @ModelAttribute("productoDTO") ProductoUpdateDTO productoDTO,
                                        BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        // --- LÍNEAS DE DIAGNÓSTICO ---
        System.out.println("--- INTENTANDO ACTUALIZAR PRODUCTO ---");
        System.out.println("ID a actualizar: " + id);
        System.out.println("Datos recibidos en DTO: " + productoDTO.toString());
        // -----------------------------

        if (bindingResult.hasErrors()) {
            System.out.println("Error de validación: " + bindingResult.getAllErrors());
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("titulo", "Editar Producto");
            model.addAttribute("isEditMode", true);
            return "producto-form";
        }
        try {
            productoDTO.setId(id);
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