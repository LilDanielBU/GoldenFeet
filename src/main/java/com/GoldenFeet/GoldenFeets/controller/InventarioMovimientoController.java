package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.HistorialDTO;
import com.GoldenFeet.GoldenFeets.dto.IngresoDTO;
import com.GoldenFeet.GoldenFeets.entity.VarianteProducto;
import com.GoldenFeet.GoldenFeets.repository.VarianteProductoRepository;
import com.GoldenFeet.GoldenFeets.service.InventarioMovimientoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

/**
 * Controlador REST para gestionar las operaciones de movimientos de inventario.
 */
@RestController
@RequestMapping("/api/inventario") // Todas las URLs de este controlador empezarán con /api/inventario
@CrossOrigin(origins = "*")
public class InventarioMovimientoController {

    private final InventarioMovimientoService inventarioMovimientoService;
    // Necesitas inyectar el repositorio de variantes para consultar stock directamente desde el controlador si es necesario
    private final VarianteProductoRepository varianteRepository;

    @Autowired
    public InventarioMovimientoController(InventarioMovimientoService inventarioMovimientoService,
                                          VarianteProductoRepository varianteRepository) {
        this.inventarioMovimientoService = inventarioMovimientoService;
        this.varianteRepository = varianteRepository;
    }

    /**
     * Endpoint para REGISTRAR UN INGRESO de stock.
     */
    @PostMapping("/ingreso")
    public ResponseEntity<String> registrarIngreso(@RequestBody IngresoDTO ingresoDTO) {
        // El DTO debe contener el ID de la VARIANTE (antes era productoId)
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

    // ✔ MÉTODO AÑADIDO: Registrar Salida (Necesario para el flujo de inventario)
    @PostMapping("/salida")
    public ResponseEntity<String> registrarSalida(@RequestBody IngresoDTO salidaDTO) {
        try {
            inventarioMovimientoService.registrarSalida(salidaDTO);
            return ResponseEntity.ok("Salida registrada exitosamente.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar la salida: " + e.getMessage());
        }
    }

    // ✔ MÉTODO AÑADIDO: Obtener STOCK actual de la VARIANTE
    @GetMapping("/stock/{varianteId}")
    // Aquí el ID en la URL es el ID de la Variante
    public ResponseEntity<?> obtenerStockVariante(@PathVariable("varianteId") Long varianteId) {
        try {
            VarianteProducto variante = varianteRepository.findById(varianteId)
                    .orElseThrow(() -> new EntityNotFoundException("Variante no encontrada con ID: " + varianteId));

            // Devolvemos el stock de la variante
            return ResponseEntity.ok(variante.getStock());

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Variante no encontrada.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener stock.");
        }
    }


    /**
     * Endpoint para OBTENER EL HISTORIAL de una VARIANTE.
     */
    @GetMapping("/historial/{varianteId}")
    // El nombre de la variable de path debería ser `varianteId` para ser claro.
    public ResponseEntity<?> getHistorialPorProducto(@PathVariable Integer varianteId) {
        try {
            // El servicio (ServiceImpl) ya fue corregido para buscar por ID de variante
            List<HistorialDTO> historial = inventarioMovimientoService.getHistorialPorProducto(varianteId);
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al consultar el historial: " + e.getMessage());
        }
    }
}