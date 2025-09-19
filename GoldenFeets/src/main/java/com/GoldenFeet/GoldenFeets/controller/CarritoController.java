package com.GoldenFeet.GoldenFeets.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CarritoController {

    @GetMapping("/carrito")
    public String verCarrito() {
        // La lógica del carrito la manejaremos con JS y API REST más adelante
        return "carrito";
    }

    @GetMapping("/deseos")
    public String verListaDeseos() {
        // Similar al carrito, la lógica puede ser del lado del cliente o con API
        return "lista_deseos";
    }
}