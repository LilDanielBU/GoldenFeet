// ruta: src/main/java/com/GoldenFeet/GoldenFeets/controller/CarritoController.java
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
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CarritoController {

    private final ProductoService productoService;

    @GetMapping("/carrito")
    public String verCarrito(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> carritoMap = (Map<Long, Integer>) session.getAttribute("carrito");

        List<CarritoItemDTO> itemsDelCarrito = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        if (carritoMap != null && !carritoMap.isEmpty()) {
            for (Map.Entry<Long, Integer> entry : carritoMap.entrySet()) {
                Long productoId = entry.getKey();
                Integer cantidad = entry.getValue();
                Optional<ProductoDTO> productoOpt = productoService.buscarPorId(productoId);

                if (productoOpt.isPresent()) {
                    ProductoDTO producto = productoOpt.get();
                    BigDecimal precioItem = producto.precio().multiply(new BigDecimal(cantidad));
                    itemsDelCarrito.add(new CarritoItemDTO(producto, cantidad, precioItem));
                    subtotal = subtotal.add(precioItem);
                }
            }
        }

        // Puedes añadir costos de envío y otros cálculos aquí si lo necesitas
        BigDecimal total = subtotal; // Por ahora, el total es igual al subtotal

        model.addAttribute("itemsCarrito", itemsDelCarrito);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("total", total);

        return "carrito"; // Devuelve la plantilla carrito.html
    }
}