package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.GoldenFeet.GoldenFeets.entity.Usuario; // <-- Importar la entidad Usuario

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired
    private UsuarioService usuarioService;

    // --- 1. Endpoint para Eliminar Usuario (DELETE) ---

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuarioApi(@PathVariable Integer id) {
        try {
            usuarioService.eliminarUsuario(id);
            // Si tiene éxito, retorna un HTTP 200 OK sin cuerpo
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Si falla, retorna un HTTP 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al eliminar usuario: " + e.getMessage());
        }
    }

    // --- 2. Endpoint para Cargar Usuarios (GET) con Filtrado ---
    // Coincide con la llamada fetch('/api/admin/usuarios') de cargarUsuarios()
    @GetMapping("/usuarios")
    public ResponseEntity<?> obtenerTodosLosUsuarios() {

        List<Usuario> todosLosUsuarios = usuarioService.obtenerTodosLosUsuarios();

        List<Usuario> usuariosAdministrativos = todosLosUsuarios.stream()
                .filter(usuario ->
                        usuario.getRoles().stream()
                                .anyMatch(rol -> !rol.getNombre().equals("ROLE_CLIENTE"))
                )
                .collect(Collectors.toList());


        List<Map<String, Object>> usuariosDto = usuariosAdministrativos.stream()
                .map(usuario -> Map.of(
                        "id", usuario.getIdUsuario(),
                        "nombre", usuario.getNombre() != null ? usuario.getNombre() : "",
                        "email", usuario.getEmail() != null ? usuario.getEmail() : "",
                        "localidad", usuario.getLocalidad() != null ? usuario.getLocalidad() : "",
                        "telefono", usuario.getTelefono() != null ? usuario.getTelefono() : "",

                        "activo", usuario.isActivo(),
                        "roles", usuario.getRoles().stream()
                                .map(rol -> rol.getNombre().toUpperCase())
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(usuariosDto);
    }

    // --- 3. Endpoint para Cargar Estadísticas (GET) ---

    // Coincide con la llamada fetch('/api/admin/stats') de cargarEstadisticas()
    // NOTA: Debes implementar la lógica real en tu UsuarioService y VentaService
    @GetMapping("/stats")
    public ResponseEntity<?> obtenerEstadisticas() {
        // Lógica de ejemplo, DEBES SUSTITUIRLA con los métodos de tus Services
        // long totalProductos = productoService.contarProductos(); // Ejemplo real
        // long comprasPendientes = ventaService.contarComprasPorEstado("PENDIENTE"); // Ejemplo real

        long totalProductos = 150;
        long comprasPendientes = 8;
        long usuariosActivos = usuarioService.contarUsuariosActivos();
        String valorInventario = "12,500.00";

        Map<String, Object> stats = Map.of(
                "totalProductos", totalProductos,
                "comprasPendientes", comprasPendientes,
                "usuariosActivos", usuariosActivos,
                "valorInventario", valorInventario
        );

        return ResponseEntity.ok(stats);
    }
}