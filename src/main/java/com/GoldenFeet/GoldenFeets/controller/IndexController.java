package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.service.CategoriaService;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    @GetMapping("/")
    public String verPaginaDeInicio(Model model) {

        // 1. Obtenemos los productos destacados y los añadimos al modelo
        // Esto hace que la variable "productosDestacados" esté disponible para tu plantilla Thymeleaf.
        model.addAttribute("productosDestacados", productoService.obtenerProductosRecientes(4));

        // 2. Obtenemos todas las categorías y las añadimos al modelo
        // Esto hace que la variable "categorias" esté disponible para tu plantilla Thymeleaf.
        model.addAttribute("categorias", categoriaService.listarTodas());

        return "index"; // Devuelve el nombre de tu archivo de plantilla: "index.html"
    }
}