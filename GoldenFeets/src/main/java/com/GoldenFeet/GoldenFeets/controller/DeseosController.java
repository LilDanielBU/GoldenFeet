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
import java.util.Optional; // <-- AÑADE ESTA LÍNEA
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DeseosController {

    private final ProductoService productoService;

    @GetMapping("/lista_deseos")
    public String verListaDeseos(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        Set<Long> deseosIds = (Set<Long>) session.getAttribute("deseos");

        if (deseosIds == null || deseosIds.isEmpty()) {
            model.addAttribute("productosDeseados", Collections.emptyList());
        } else {
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