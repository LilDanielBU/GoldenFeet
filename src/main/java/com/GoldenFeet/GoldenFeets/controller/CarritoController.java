package com.GoldenFeet.GoldenFeets.controller;

// --- Importaciones necesarias ---
import com.GoldenFeet.GoldenFeets.dto.CarritoItemDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.entity.Usuario; // ¡Importante! Importa tu entidad Usuario
import com.GoldenFeet.GoldenFeets.service.UsuarioService; // ¡Importante! Importa tu servicio de Usuario
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // Para obtener el usuario
import org.springframework.security.core.context.SecurityContextHolder; // Para obtener el usuario
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
@RequiredArgsConstructor // Lombok se encargará de inyectar ambos servicios
public class CarritoController {

    private final ProductoService productoService;
    private final UsuarioService usuarioService; // 1. Añadimos el servicio de usuario

    @GetMapping("/carrito")
    public String verCarrito(HttpSession session, Model model) {

        // --- LÓGICA MEJORADA PARA VERIFICAR AUTENTICACIÓN ---
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean usuarioAutenticado = authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getName());

        model.addAttribute("usuarioAutenticado", usuarioAutenticado);

        // Si el usuario SÍ está autenticado, cargamos sus datos para autocompletar el formulario
        if (usuarioAutenticado) {
            String userEmail = authentication.getName();
            Usuario usuario = usuarioService.buscarPorEmail(userEmail);
            if (usuario != null) {
                model.addAttribute("usuario", usuario);
            }
        }

        // ===================================================================
        // PARTE 2: LÓGICA DEL CARRITO (TU CÓDIGO ORIGINAL, ESTÁ PERFECTO)
        // ===================================================================
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> carritoMap = (Map<Integer, Integer>) session.getAttribute("carrito");

        List<CarritoItemDTO> itemsDelCarrito = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        if (carritoMap != null && !carritoMap.isEmpty()) {
            List<Integer> productoIds = new ArrayList<>(carritoMap.keySet());
            List<ProductoDTO> productosEncontrados = productoService.listarPorIds(productoIds);

            Map<Integer, ProductoDTO> productosMap = productosEncontrados.stream()
                    .collect(Collectors.toMap(ProductoDTO::getId, Function.identity()));

            for (Map.Entry<Integer, Integer> entry : carritoMap.entrySet()) {
                Integer productoId = entry.getKey();
                Integer cantidad = entry.getValue();
                ProductoDTO producto = productosMap.get(productoId);

                if (producto != null) {
                    BigDecimal precioItem = producto.getPrecio().multiply(new BigDecimal(cantidad));
                    itemsDelCarrito.add(new CarritoItemDTO(producto, cantidad, precioItem));
                    subtotal = subtotal.add(precioItem);
                }
            }
        }

        model.addAttribute("itemsCarrito", itemsDelCarrito);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("total", subtotal); // Puedes agregar lógica de envío o impuestos aquí más adelante

        return "carrito"; // Thymeleaf buscará la plantilla 'carrito.html'
    }
}