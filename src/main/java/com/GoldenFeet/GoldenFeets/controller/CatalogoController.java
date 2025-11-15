package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CatalogoController {

    private final ProductoService productoService;

    @GetMapping("/catalogo")
    public String verCatalogo(
            Model model,
            // Parámetros para los filtros del <form>
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) List<String> marcas,
            // Parámetro para la barra de búsqueda
            @RequestParam(required = false) String busqueda) {

        List<ProductoDTO> productos;
        String titulo;

        // La búsqueda por nombre tiene la máxima prioridad
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            productos = productoService.buscarPorNombre(busqueda);
            titulo = "Resultados para: '" + busqueda + "'";
        } else {
            // Si no hay búsqueda, usamos el método de filtrado avanzado
            productos = productoService.filtrarProductos(categoria, precioMax, marcas);

            // Lógica para el título dinámico
            if (categoria != null) {
                titulo = "Categoría: " + categoria;
            } else if (marcas != null && !marcas.isEmpty()) {
                titulo = "Marcas seleccionadas";
            } else {
                titulo = "Nuestro Catálogo";
            }
        }

        // --- Pasamos todos los datos necesarios a la vista ---
        model.addAttribute("productos", productos);
        model.addAttribute("tituloCatalogo", titulo);
        model.addAttribute("productosDestacados", productoService.listarDestacados());
        model.addAttribute("categorias", productoService.listarCategorias());

        // Es necesario pasar la lista de marcas disponibles para construir los checkboxes en el HTML
        model.addAttribute("marcasDisponibles", productoService.listarMarcasDistintas());

        return "catalogo";
    }
}