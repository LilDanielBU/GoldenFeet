package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor // Usa esto preferiblemente para inyección de dependencias final
public class ApiController {

    private final ProductoService productoService;

    /**
     * ✅ ENDPOINT CLAVE PARA EL MODAL
     * Obtiene el producto y sus variantes (Tallas, Colores, Stock real).
     */
    @GetMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> obtenerProducto(@PathVariable Long id) {
        // Usamos el método que creamos en el paso anterior para incluir las variantes
        ProductoDTO producto = productoService.obtenerProductoConVariantes(id);

        if (producto != null) {
            return ResponseEntity.ok(producto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Recibe una lista de IDs y devuelve los detalles.
     * Útil para refrescar el carrito si usas LocalStorage.
     */
    @PostMapping("/productos/by-ids")
    public ResponseEntity<List<ProductoDTO>> obtenerProductosPorIds(@RequestBody List<Long> ids) {
        // Asegúrate de que tu servicio tenga este método aceptando List<Long>
        List<ProductoDTO> productos = productoService.listarPorIds(ids);
        return ResponseEntity.ok(productos);
    }

    // ========================================================================
    // SECCIÓN DE CARRITO DE SESIÓN (Opcional si usas LocalStorage en el front)
    // ========================================================================

    /**
     * Agrega un producto al carrito de compras almacenado en la sesión (Backend Session).
     */
    @PostMapping("/carrito/agregar")
    public Map<String, Object> agregarAlCarrito(@RequestBody ItemCarritoRequest request, HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute("carrito");

        if (carrito == null) {
            carrito = new HashMap<>();
        }

        Long productoId = request.getProductoId();
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
     * Obtiene el número total de items en el carrito de sesión.
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
 * DTO interno para solicitudes de carrito
 * (Puedes moverlo a un archivo separado en el paquete dto si prefieres)
 */
@Data
class ItemCarritoRequest {
    private Long productoId; // Cambiado a Long por consistencia
    private int cantidad;
}