package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List; // <-- Importación necesaria para el nuevo método
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ApiController {

    private final ProductoService productoService;

    /**
     * Obtiene los detalles de un producto por su ID.
     * Usado para la "Vista Rápida" en el catálogo.
     */
    @GetMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> obtenerProducto(@PathVariable Long id) {
        return productoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- ENDPOINT AÑADIDO PARA LA LISTA DE DESEOS ---
    /**
     * Recibe una lista de IDs de productos y devuelve los detalles de cada uno.
     * Esencial para que la página de Lista de Deseos pueda mostrar los productos.
     */
    @PostMapping("/productos/by-ids")
    public ResponseEntity<List<ProductoDTO>> obtenerProductosPorIds(@RequestBody List<Long> ids) {
        List<ProductoDTO> productos = productoService.listarPorIds(ids);
        return ResponseEntity.ok(productos);
    }
    // --- FIN DEL CÓDIGO AÑADIDO ---


    /**
     * Agrega un producto al carrito de compras almacenado en la sesión.
     */
    @PostMapping("/carrito/agregar")
    public Map<String, Object> agregarAlCarrito(@RequestBody ItemCarritoRequest request, HttpSession session) {
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

    /**
     * Obtiene el número total de items en el carrito.
     */
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

/**
 * Clase DTO para recibir la solicitud de agregar un item al carrito.
 */
@Data
class ItemCarritoRequest {
    private long productoId;
    private int cantidad;
}