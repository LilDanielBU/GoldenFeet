package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.CrearVentaRequestDTO;
import com.GoldenFeet.GoldenFeets.dto.VentaResponseDTO;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.service.VentaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final VentaService ventaService;

    @PostMapping("/crear")
    public ResponseEntity<?> crearNuevoPedido(@RequestBody CrearVentaRequestDTO pedidoRequest,
                                              @AuthenticationPrincipal Usuario usuario) {

        // 1. Verificamos que el usuario haya iniciado sesión
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debes iniciar sesión para realizar un pedido."));
        }

        // 2. Verificamos que el carrito no esté vacío
        if (pedidoRequest == null || pedidoRequest.items() == null || pedidoRequest.items().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El carrito está vacío. No se puede procesar el pedido."));
        }

        try {
            // 3. Llamamos al servicio con el DTO y el email del usuario autenticado
            VentaResponseDTO nuevaVenta = ventaService.crearVenta(pedidoRequest, usuario.getUsername());

            // 4. Devolvemos una respuesta exitosa con los datos de la venta creada
            return ResponseEntity.ok(nuevaVenta);

        } catch (EntityNotFoundException | IllegalStateException e) {
            // Capturamos errores específicos de negocio (ej. producto no encontrado, sin stock)
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Capturamos cualquier otro error inesperado
            e.printStackTrace(); // Es buena práctica registrar el error completo en el servidor
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ocurrió un error inesperado al procesar el pedido."));
        }
    }
}