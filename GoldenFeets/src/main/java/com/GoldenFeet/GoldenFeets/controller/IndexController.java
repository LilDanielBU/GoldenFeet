package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor // Facilita la inyección de dependencias
public class IndexController {

    // 1. Inyectamos el servicio para poder usarlo
    private final ProductoService productoService;

    @GetMapping("/")
    public String verPaginaDeInicio(Model model) { // 2. Añadimos 'Model' para poder pasar datos

        // 3. Obtenemos las categorías y las añadimos al modelo
        // Esto le da a Thymeleaf la variable "categorias" que necesita
        model.addAttribute("categorias", productoService.listarCategorias());

        return "index"; // 4. Devolvemos el nombre de la plantilla
    }
}