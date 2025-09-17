package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller // ¡OJO! Usa @Controller, no @RestController, porque devuelves una vista HTML.
@RequiredArgsConstructor
public class HomeController {

    private final ProductoService productoService;

    @GetMapping("/") // Escucha las peticiones a la raíz del sitio (ej: http://localhost:8080/)
    public String home(Model model) {

        // 1. Pide la lista de categorías al servicio
        List<CategoriaDTO> categorias = productoService.listarCategorias();

        // 2. Agrega la lista al "modelo" para que Thymeleaf pueda usarla
        model.addAttribute("categorias", categorias);

        // 3. Devuelve el nombre del archivo HTML que se debe mostrar (sin la extensión)
        return "index";
    }
}