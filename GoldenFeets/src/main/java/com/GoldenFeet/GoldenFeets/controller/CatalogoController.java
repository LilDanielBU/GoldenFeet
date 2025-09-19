package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CatalogoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping("/catalogo")
    public String verCatalogo(Model model) {
        // CORRECCIÓN: Se usa el nuevo nombre de método 'listarTodos()'
        model.addAttribute("productos", productoService.listarTodos());

        // CORRECCIÓN: Se usa el nuevo nombre de método 'listarDestacados()'
        model.addAttribute("productosDestacados", productoService.listarDestacados());

        // También puedes cargar las categorías para los filtros
        model.addAttribute("categorias", productoService.listarCategorias());

        return "catalogo"; // Devuelve el nombre del archivo HTML (sin la extensión)
    }
}