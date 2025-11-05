package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.HistorialDTO;
import com.GoldenFeet.GoldenFeets.dto.IngresoDTO;
import com.GoldenFeet.GoldenFeets.service.InventarioMovimientoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

/**
 * Controlador REST para gestionar las operaciones de movimientos de inventario.
 * Expone los endpoints para que el frontend pueda interactuar con el servicio.
 */
@RestController
@RequestMapping("/api/inventario") // Todas las URLs de este controlador empezarán con /api/inventario
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen (frontend)
public class InventarioMovimientoController {

    private final InventarioMovimientoService inventarioMovimientoService;

    // Inyectamos el servicio que contiene la lógica
    @Autowired
    public InventarioMovimientoController(InventarioMovimientoService inventarioMovimientoService) {
        this.inventarioMovimientoService = inventarioMovimientoService;
    }

    /**
     * Endpoint para REGISTRAR UN INGRESO de stock.
     * Escucha en: POST http://localhost:8080/api/inventario/ingreso
     *
     * @param ingresoDTO El JSON que envía el frontend con {productoId, cantidad, motivo}
     * @return Una respuesta HTTP.
     */
    @PostMapping("/ingreso")
    public ResponseEntity<String> registrarIngreso(@RequestBody IngresoDTO ingresoDTO) {
        // Este método está perfecto, no necesita cambios.
        try {
            inventarioMovimientoService.registrarIngreso(ingresoDTO);
            return ResponseEntity.ok("Ingreso registrado exitosamente.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar el ingreso: " + e.getMessage());
        }
    }

    /**
     * Endpoint para OBTENER EL HISTORIAL de un producto.
     * Escucha en: GET http://localhost:8080/api/inventario/historial/1 (donde 1 es el ID del producto)
     *
     * @param productoId El ID del producto que viene en la URL.
     * @return Una lista del historial (HistorialDTO) o un error.
     */
    @GetMapping("/historial/{productoId}")
    // --- INICIO DE CORRECCIÓN ---
    // 1. Cambiamos @PathVariable Long a Integer para que coincida con el servicio.
    public ResponseEntity<?> getHistorialPorProducto(@PathVariable Integer productoId) {
        // --- FIN DE CORRECCIÓN ---
        try {
            // 2. Ya no necesitamos Math.toIntExact(), pasamos el Integer directamente.
            List<HistorialDTO> historial = inventarioMovimientoService.getHistorialPorProducto(productoId);
            return ResponseEntity.ok(historial); // Devuelve la lista como JSON con estado 200 OK
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al consultar el historial: " + e.getMessage());
        }
    }
}