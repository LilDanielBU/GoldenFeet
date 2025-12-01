package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.IngresoDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoCreateDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoUpdateDTO;
import com.GoldenFeet.GoldenFeets.entity.Categoria;
import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository;
import com.GoldenFeet.GoldenFeets.repository.InventarioMovimientoRepository;
import com.GoldenFeet.GoldenFeets.service.AlmacenamientoService;
import com.GoldenFeet.GoldenFeets.service.InventarioMovimientoService;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import com.GoldenFeet.GoldenFeets.service.ReporteInventarioPDF; // Importante: Tu clase util

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/inventario")
@PreAuthorize("hasRole('GERENTEINVENTARIO')")
@RequiredArgsConstructor
public class InventarioController {

    private final ProductoService productoService;
    private final CategoriaRepository categoriaRepository;
    private final AlmacenamientoService almacenamientoService;
    private final InventarioMovimientoService inventarioMovimientoService;
    private final InventarioMovimientoRepository inventarioMovimientoRepository; // Inyectado para el reporte

    // --- LISTAS ESTÁTICAS PARA LOS SELECTS ---
    private List<Integer> obtenerTallas() {
        return IntStream.rangeClosed(30, 41).boxed().collect(Collectors.toList());
    }

    private List<String> obtenerColores() {
        return List.of("Negro", "Blanco", "Rojo", "Azul", "Verde", "Gris", "Beige", "Cafe", "Rosa", "Amarillo", "Multicolor");
    }

    @GetMapping("/panel")
    public String mostrarPanelInventario(Model model) {
        List<ProductoDTO> listaProductos = productoService.listarTodos();
        model.addAttribute("productos", listaProductos);
        model.addAttribute("titulo", "Panel de Gestión de Inventario");

        // KPIs del Dashboard
        model.addAttribute("totalProductos", listaProductos.size());
        model.addAttribute("productosAgotados", listaProductos.stream().filter(p -> p.getStock() == 0).count());
        model.addAttribute("productosStockBajo", listaProductos.stream().filter(p -> p.getStock() > 0 && p.getStock() < 5).count());
        model.addAttribute("valorTotalInventario", productoService.calcularValorTotalInventario());

        if (!model.containsAttribute("ingresoDTO")) {
            model.addAttribute("ingresoDTO", new IngresoDTO());
        }

        return "inventario-panel";
    }

    @GetMapping("/productos/nuevo")
    public String mostrarFormularioDeProducto(Model model) {
        List<Categoria> categorias = categoriaRepository.findAll();

        model.addAttribute("productoDTO", new ProductoCreateDTO());
        model.addAttribute("categorias", categorias);

        // ENVIAMOS LAS LISTAS A LA VISTA
        model.addAttribute("tallas", obtenerTallas());
        model.addAttribute("colores", obtenerColores());

        model.addAttribute("titulo", "Registrar Nuevo Producto");
        return "producto-form";
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(@Valid @ModelAttribute("productoDTO") ProductoCreateDTO productoDTO,
                                  BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaRepository.findAll());
            // SI FALLA, VOLVEMOS A ENVIAR LAS LISTAS
            model.addAttribute("tallas", obtenerTallas());
            model.addAttribute("colores", obtenerColores());
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

        // Mapeo manual
        updateDTO.setId(productoDTO.getId());
        updateDTO.setNombre(productoDTO.getNombre());
        updateDTO.setDescripcion(productoDTO.getDescripcion());
        updateDTO.setPrecio(productoDTO.getPrecio());
        updateDTO.setOriginalPrice(productoDTO.getOriginalPrice());
        updateDTO.setMarca(productoDTO.getMarca());
        updateDTO.setDestacado(productoDTO.getDestacado());
        updateDTO.setCategoriaId(productoDTO.getCategoriaId());

        // Nuevos campos
        updateDTO.setTalla(productoDTO.getTalla());
        updateDTO.setColor(productoDTO.getColor());

        model.addAttribute("productoDTO", updateDTO);
        model.addAttribute("categorias", categoriaRepository.findAll());

        // ENVIAMOS LAS LISTAS A LA VISTA
        model.addAttribute("tallas", obtenerTallas());
        model.addAttribute("colores", obtenerColores());

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
            // SI FALLA, VOLVEMOS A ENVIAR LAS LISTAS
            model.addAttribute("tallas", obtenerTallas());
            model.addAttribute("colores", obtenerColores());
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

    // Métodos de ingreso/salida stock
    @PostMapping("/ingreso-stock")
    public String registrarIngresoStock(@Valid @ModelAttribute("ingresoDTO") IngresoDTO ingresoDTO,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error de validación.");
            return "redirect:/inventario/panel";
        }
        try {
            inventarioMovimientoService.registrarIngreso(ingresoDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Stock agregado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/inventario/panel";
    }

    @PostMapping("/salida-stock")
    public String registrarSalidaStock(@Valid @ModelAttribute("ingresoDTO") IngresoDTO salidaDTO,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error de validación.");
            return "redirect:/inventario/panel";
        }
        try {
            inventarioMovimientoService.registrarSalida(salidaDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Stock retirado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/inventario/panel";
    }

    // --- NUEVO: MÉTODO PARA EXPORTAR PDF ---
    @GetMapping("/reporte/pdf")
    public void exportarListadoPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Historial_Inventario_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        // Obtenemos TODOS los movimientos ordenados por fecha descendente
        List<InventarioMovimiento> listaMovimientos = inventarioMovimientoRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha"));

        ReporteInventarioPDF exportador = new ReporteInventarioPDF(listaMovimientos);
        exportador.exportar(response);
    }
}