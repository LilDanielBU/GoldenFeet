package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DeseosController {

    private final ProductoService productoService;

    @GetMapping("/lista_deseos")
    public String verListaDeseos(HttpSession session, Model model) {

        // --- CORRECCIÓN AQUÍ ---
        // Se cambia el tipo de dato de Long a Integer para que coincida con el servicio.
        @SuppressWarnings("unchecked")
        Set<Integer> deseosIds = (Set<Integer>) session.getAttribute("deseos");

        if (deseosIds == null || deseosIds.isEmpty()) {
            model.addAttribute("productosDeseados", Collections.emptyList());
        } else {
            // Esta llamada ahora funciona porque deseosIds contiene Integers.
            List<ProductoDTO> productosDeseados = deseosIds.stream()
                    .map(productoService::buscarPorId)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            model.addAttribute("productosDeseados", productosDeseados);
        }

        return "lista_deseos";
    }
}