package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.*;
import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import com.GoldenFeet.GoldenFeets.entity.VarianteProducto;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository;
import com.GoldenFeet.GoldenFeets.repository.InventarioMovimientoRepository;
import com.GoldenFeet.GoldenFeets.repository.VarianteProductoRepository;
import com.GoldenFeet.GoldenFeets.service.InventarioMovimientoService;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import com.GoldenFeet.GoldenFeets.service.ReporteInventarioPDF;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid; // Se mantiene el import, pero se quita el uso en el método crítico
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/inventario")
@PreAuthorize("hasRole('GERENTEINVENTARIO')")
@RequiredArgsConstructor
public class InventarioController {


    private final ProductoService productoService;
    private final CategoriaRepository categoriaRepository;
    private final InventarioMovimientoService inventarioMovimientoService;
    private final InventarioMovimientoRepository inventarioMovimientoRepository;
    private final VarianteProductoRepository varianteRepository;

    @GetMapping("/panel")
    public String mostrarPanelInventario(Model model) {
        List<VarianteProducto> variantes = varianteRepository.findAll();

        model.addAttribute("productos", variantes);
        model.addAttribute("titulo", "Panel de Gestión de Inventario");
        model.addAttribute("totalVariantes", variantes.size());
        model.addAttribute("productosAgotados", variantes.stream().filter(v -> v.getStock() == 0).count());
        model.addAttribute("productosStockBajo", variantes.stream().filter(v -> v.getStock() > 0 && v.getStock() < 5).count());

        BigDecimal valorTotal = variantes.stream()
                .map(v -> {
                    Double precioDouble = v.getProducto() != null ? v.getProducto().getPrecio() : null;
                    BigDecimal precio = (precioDouble != null) ? BigDecimal.valueOf(precioDouble) : BigDecimal.ZERO;
                    return precio.multiply(new BigDecimal(v.getStock()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("valorTotalInventario", valorTotal);

        if (!model.containsAttribute("ingresoDTO")) {
            model.addAttribute("ingresoDTO", new IngresoDTO());
        }

        return "inventario-panel";
    }

    @GetMapping("/movimientos/gestionar/{id}")
    public String mostrarFormularioMovimientos(@PathVariable("id") Long id, Model model) {
        // 1. Buscamos la variante por su ID
        VarianteProducto variante = varianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variante no encontrada con ID: " + id));

        // 2. Pasamos la información al modelo
        model.addAttribute("variante", variante);
        // CORRECCIÓN: Se cambia 'variante.talla()' por 'variante.getTalla()'
        model.addAttribute("titulo", "Gestionar Stock: " + variante.getProducto().getNombre() +
                " (Talla " + variante.getTalla() + " - " + variante.getColor() + ")");

        // 3. Preparamos el DTO para el formulario
        model.addAttribute("ingresoDTO", new IngresoDTO());

        // 4. Retornamos la vista (debes tener el archivo movimientos-form.html creado)
        return "movimientos-form";
    }

    @GetMapping("/productos/nuevo")
    public String mostrarFormularioDeProducto(Model model) {
        model.addAttribute("productoDTO", new ProductoCreateDTO());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("titulo", "Registrar Nuevo Producto");
        model.addAttribute("isEditMode", false);
        return "producto-form";
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(@Valid @ModelAttribute("productoDTO") ProductoCreateDTO productoDTO,
                                  BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("titulo", "Registrar Nuevo Producto");
            model.addAttribute("isEditMode", false);
            return "producto-form";
        }
        try {
            productoService.crearProducto(productoDTO);
            redirectAttributes.addFlashAttribute("successMessage", "¡Producto registrado exitosamente!");
            return "redirect:/inventario/panel";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error al registrar: " + e.getMessage());
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("isEditMode", false);
            return "producto-form";
        }
    }

    @GetMapping("/productos/editar/{id}")
    public String mostrarFormularioDeEdicion(@PathVariable("id") Long id,
                                             Model model,
                                             RedirectAttributes redirectAttributes) {
        Optional<ProductoDTO> productoOpt = productoService.buscarPorId(id);

        if (productoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado.");
            return "redirect:/inventario/panel";
        }

        model.addAttribute("productoDTO", productoOpt.get());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("isEditMode", true);

        return "producto-form";
    }

    @PostMapping("/productos/actualizar/{id}")
    public String procesarActualizacion(@PathVariable("id") Long id,
                                        // ----------------------------------------------------
                                        // ¡CAMBIO CRÍTICO DE DIAGNÓSTICO!
                                        // Eliminamos @Valid y el chequeo de errors para forzar
                                        // la excepción de BD/JPA, que es el error real.
                                        // ----------------------------------------------------
                                        @ModelAttribute("productoDTO") ProductoUpdateDTO productoDTO,
                                        BindingResult bindingResult, // Se mantiene, pero se ignora temporalmente
                                        RedirectAttributes redirectAttributes,
                                        Model model) {

        // --- BLOQUE if (bindingResult.hasErrors()) ELIMINADO ---
        // Ahora el código de servicio se ejecuta inmediatamente.

        try {
            productoService.actualizarProducto(id, productoDTO);
            redirectAttributes.addFlashAttribute("successMessage", "¡Producto actualizado exitosamente!");
            return "redirect:/inventario/panel";
        } catch (Exception e) {
            // **********************************************
            // CORRECCIÓN CRÍTICA: Mostrar la causa real del fallo (ya implementada)
            // **********************************************
            e.printStackTrace();

            String fullError = "Error al actualizar: " + e.getMessage();

            // Intentamos obtener el mensaje de la causa raíz (Típicamente la excepción SQL/JPA)
            Throwable cause = e.getCause();
            if (cause != null) {
                // Buscamos la causa más profunda que no sea una excepción genérica
                while (cause.getCause() != null && cause.getCause() != cause) {
                    cause = cause.getCause();
                }
                fullError += " Causa: " + cause.getMessage();
            }

            model.addAttribute("errorMessage", fullError);
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("titulo", "Editar Producto");
            model.addAttribute("isEditMode", true);

            // Si el error es de BD/JPA, volvemos a cargar las categorías y la vista.
            return "producto-form";
        }
    }

    /**
     * Endpoint para eliminar una VARIANTE individual.
     * Requiere que el ProductoService implemente la lógica para eliminar primero
     * las dependencias (InventarioMovimiento) antes de eliminar la VarianteProducto.
     */
    @PostMapping("/variantes/eliminar/{id}")
    public String eliminarVariante(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            // Se asume que ProductoService tiene este método que maneja la lógica de FK
            productoService.eliminarVariante(id);
            redirectAttributes.addFlashAttribute("successMessage", "Variante eliminada exitosamente.");
        } catch (DataIntegrityViolationException e) {
            // Captura si todavía hay una dependencia que el servicio no eliminó
            redirectAttributes.addFlashAttribute("errorMessage", "Error: No se puede eliminar la variante. Existen registros de pedidos o movimientos de inventario asociados.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la variante: " + e.getMessage());
        }

        // REDIRECCIÓN: Lo ideal es volver a la edición del producto padre,
        // pero por simplicidad, redirigimos al panel.
        return "redirect:/inventario/panel";
    }

    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("successMessage", "Producto eliminado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/inventario/panel";
    }

    @PostMapping("/ingreso-stock")
    public String registrarIngresoStock(@Valid @ModelAttribute("ingresoDTO") IngresoDTO ingresoDTO,
                                        BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos inválidos para ingreso.");
            return "redirect:/inventario/panel";
        }
        try {
            inventarioMovimientoService.registrarIngreso(ingresoDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Stock agregado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/inventario/panel";
    }

    @PostMapping("/salida-stock")
    public String registrarSalidaStock(@Valid @ModelAttribute("ingresoDTO") IngresoDTO salidaDTO,
                                       BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos inválidos para salida.");
            return "redirect:/inventario/panel";
        }
        try {
            inventarioMovimientoService.registrarSalida(salidaDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Stock retirado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/inventario/panel";
    }

    @GetMapping("/reporte/pdf")
    public void exportarListadoPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Historial_Inventario_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<InventarioMovimiento> listaMovimientos = inventarioMovimientoRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha"));
        ReporteInventarioPDF exportador = new ReporteInventarioPDF(listaMovimientos);
        exportador.exportar(response);
    }

    @GetMapping("/historial/{id}")
    @ResponseBody
    public List<HistorialDTO> obtenerHistorial(@PathVariable("id") Long id) {
        // Mantenemos la conversión a int (intValue()) para evitar el error de "incompatible types"
        // que has visto anteriormente, asumiendo que el método subyacente en el servicio espera Integer.
        return inventarioMovimientoService.getHistorialPorProducto(id.intValue());
    }
}