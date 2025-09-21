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
        Map<Long, Integer> carritoMap = (Map<Long, Integer>) session.getAttribute("carrito");

        List<CarritoItemDTO> itemsDelCarrito = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        if (carritoMap != null && !carritoMap.isEmpty()) {
            // 1. Obtenemos todos los IDs de los productos del carrito.
            List<Long> productoIds = new ArrayList<>(carritoMap.keySet());

            // 2. Hacemos UNA SOLA CONSULTA a la base de datos para traer todos los productos.
            List<ProductoDTO> productosEncontrados = productoService.listarPorIds(productoIds);

            // 3. Convertimos la lista de productos en un mapa para un acceso rápido por ID.
            Map<Long, ProductoDTO> productosMap = productosEncontrados.stream()
                    .collect(Collectors.toMap(ProductoDTO::id, Function.identity()));

            // 4. Ahora recorremos el carrito original que tiene las cantidades.
            for (Map.Entry<Long, Integer> entry : carritoMap.entrySet()) {
                Long productoId = entry.getKey();
                Integer cantidad = entry.getValue();
                ProductoDTO producto = productosMap.get(productoId); // Obtenemos el producto del mapa (muy rápido)

                if (producto != null) {
                    BigDecimal precioItem = producto.precio().multiply(new BigDecimal(cantidad));
                    itemsDelCarrito.add(new CarritoItemDTO(producto, cantidad, precioItem));
                    subtotal = subtotal.add(precioItem);
                }
            }
        }

        model.addAttribute("itemsCarrito", itemsDelCarrito);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("total", subtotal); // Por ahora, el total es igual al subtotal

        return "carrito";
    }
}