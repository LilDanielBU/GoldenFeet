package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ApiController {

    private final ProductoService productoService;

    // --- CORRECCIÓN 1: El ID del producto ahora es Long ---
    @GetMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> obtenerProducto(@PathVariable Long id) {
        Optional<ProductoDTO> productoOpt = productoService.buscarPorId(id);
        return productoOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/carrito/agregar")
    public Map<String, Object> agregarAlCarrito(@RequestBody ItemCarritoRequest request, HttpSession session) {
        // --- CORRECCIÓN 2: El carrito ahora usa Long como clave para el ID del producto ---
        // Esto también soluciona el aviso "unchecked or unsafe operations"
        @SuppressWarnings("unchecked")
        Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new HashMap<>();
        }

        long productoId = request.getProductoId();
        int cantidadActual = carrito.getOrDefault(productoId, 0);
        carrito.put(productoId, cantidadActual + request.getCantidad());

        session.setAttribute("carrito", carrito);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", "success");
        respuesta.put("mensaje", "Producto añadido al carrito");

        int totalItems = carrito.values().stream().mapToInt(Integer::intValue).sum();
        respuesta.put("totalItems", totalItems);

        return respuesta;
    }

    @GetMapping("/carrito/total")
    public Map<String, Object> obtenerTotalCarrito(HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute("carrito");
        int totalItems = 0;
        if (carrito != null) {
            totalItems = carrito.values().stream().mapToInt(Integer::intValue).sum();
        }
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("totalItems", totalItems);
        return respuesta;
    }
}

// --- CORRECCIÓN 3: El productoId ahora es Long ---
@Data
class ItemCarritoRequest {
    private long productoId; // Usamos long para que coincida con el ID del Producto
    private int cantidad;
}