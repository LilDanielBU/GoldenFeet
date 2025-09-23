package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.CarritoItemDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CarritoController {

    private final ProductoService productoService;

    @GetMapping("/carrito")
    public String verCarrito(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> carritoMap = (Map<Integer, Integer>) session.getAttribute("carrito");

        List<CarritoItemDTO> itemsDelCarrito = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        if (carritoMap != null && !carritoMap.isEmpty()) {
            List<Integer> productoIds = new ArrayList<>(carritoMap.keySet());
            List<ProductoDTO> productosEncontrados = productoService.listarPorIds(productoIds);

            // --- CORRECCIÓN 1: Usar el método getter getId() ---
            Map<Integer, ProductoDTO> productosMap = productosEncontrados.stream()
                    .collect(Collectors.toMap(ProductoDTO::getId, Function.identity()));

            for (Map.Entry<Integer, Integer> entry : carritoMap.entrySet()) {
                Integer productoId = entry.getKey();
                Integer cantidad = entry.getValue();
                ProductoDTO producto = productosMap.get(productoId);

                if (producto != null) {
                    // --- CORRECCIÓN 2: Usar el método getter getPrecio() ---
                    BigDecimal precioItem = producto.getPrecio().multiply(new BigDecimal(cantidad));
                    itemsDelCarrito.add(new CarritoItemDTO(producto, cantidad, precioItem));
                    subtotal = subtotal.add(precioItem);
                }
            }
        }

        model.addAttribute("itemsCarrito", itemsDelCarrito);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("total", subtotal);

        return "carrito";
    }
}