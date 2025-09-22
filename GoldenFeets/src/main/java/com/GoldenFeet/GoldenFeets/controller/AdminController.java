package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.AdminUsuarioDTO;
import com.GoldenFeet.GoldenFeets.dto.AdminUsuarioUpdateDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioRegistroDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioResponseDTO;
import com.GoldenFeet.GoldenFeets.entity.Venta;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.service.VentaService;
import com.GoldenFeet.GoldenFeets.service.UsuarioService;
import com.GoldenFeet.GoldenFeets.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final VentaService ventaService;
    private final UsuarioService usuarioService;
    private final RolService rolService;

    // ===============================
    // PANEL PRINCIPAL DE ADMINISTRACIÓN
    // ===============================

    @GetMapping("/panel")
    public String mostrarPanel(Model model) {
        // Asumiendo que VentaService ya tiene el método obtenerTodasLasVentas()
        List<Venta> todasLasVentas = ventaService.obtenerTodasLasVentas();
        List<Usuario> todosLosUsuarios = usuarioService.obtenerTodosLosUsuarios();

        List<Usuario> clientes = todosLosUsuarios.stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_CLIENTE")))
                .collect(Collectors.toList());

        List<Usuario> empleados = todosLosUsuarios.stream()
                .filter(u -> u.getRoles().stream().anyMatch(r ->
                        r.getNombre().equals("ROLE_EMPLEADO") ||
                                r.getNombre().equals("ROLE_DISTRIBUIDOR")))
                .collect(Collectors.toList());

        // CORRECCIÓN: Usar map(Venta::getTotal) y sumar con .reduce() para BigDecimals
        BigDecimal totalIngresos = todasLasVentas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalVentas = todasLasVentas.size();
        int totalClientes = clientes.size();
        int totalEmpleados = empleados.size();

        // CORRECCIÓN: Usar getFechaVenta() para ordenar
        List<Venta> ventasRecientes = todasLasVentas.stream()
                .sorted((v1, v2) -> v2.getFechaVenta().compareTo(v1.getFechaVenta()))
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalEmpleados", totalEmpleados);
        model.addAttribute("ventasRecientes", ventasRecientes);
        model.addAttribute("ventas", todasLasVentas);
        model.addAttribute("clientes", clientes);
        model.addAttribute("empleados", empleados);

        return "admin-panel";
    }

    @GetMapping("/administrador")
    public String panelAdmin() {
        return "redirect:/admin/panel";
    }

    // ===============================
    // GESTIÓN DE USUARIOS
    // ===============================

    @GetMapping("/usuarios")
    public String gestionarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodosLosUsuarios());
        model.addAttribute("nuevoUsuario", new UsuarioRegistroDTO("", "", "", null, "", "", "", "", Set.of()));
        model.addAttribute("rolesTodos", rolService.listarTodosLosRoles());
        return "admin-usuarios";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute("nuevoUsuario") UsuarioRegistroDTO dto,
                                 RedirectAttributes redirectAttributes) {
        try {
            usuarioService.guardarUsuario(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario guardado exitosamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Integer id, Model model) {
        try {
            UsuarioResponseDTO usuarioActual = usuarioService.obtenerPerfil(id);

            List<Integer> rolesIds = usuarioActual.roles().stream()
                    .map(rolService::obtenerRolPorNombre)
                    .flatMap(Optional::stream)
                    .map(rol -> rol.getIdRol())
                    .collect(Collectors.toList());

            AdminUsuarioUpdateDTO usuarioParaForm = new AdminUsuarioUpdateDTO(
                    usuarioActual.idUsuario(),
                    usuarioActual.nombre(),
                    usuarioActual.email(),
                    usuarioActual.activo(),

                    rolesIds
            );

            model.addAttribute("usuario", usuarioParaForm);
            model.addAttribute("rolesTodos", rolService.listarTodosLosRoles());
            return "admin-usuario-edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Usuario no encontrado: " + e.getMessage());
            return "redirect:/admin/usuarios";
        }
    }

    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuario(@ModelAttribute("usuario") AdminUsuarioUpdateDTO dto,
                                    RedirectAttributes redirectAttributes) {
        try {
            usuarioService.actualizarUsuarioAdmin(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    // ===============================
    // API REST - ESTADÍSTICAS
    // ===============================

    @GetMapping("/api/estadisticas")
    @ResponseBody
    public Map<String, Object> obtenerEstadisticas(@RequestParam(defaultValue = "30") int dias) {
        LocalDate fechaInicio = LocalDate.now().minusDays(dias);
        List<Venta> ventasPeriodo = ventaService.obtenerVentasPorPeriodo(fechaInicio, LocalDate.now());

        Map<String, Object> estadisticas = new HashMap<>();

        Map<String, Integer> ventasPorMes = new LinkedHashMap<>();
        Map<String, BigDecimal> ingresosPorMes = new LinkedHashMap<>();

        for (int i = 11; i >= 0; i--) {
            LocalDate mes = LocalDate.now().minusMonths(i);
            String nombreMes = mes.format(DateTimeFormatter.ofPattern("MMM"));
            ventasPorMes.put(nombreMes, 0);
            ingresosPorMes.put(nombreMes, BigDecimal.ZERO);
        }

        ventasPeriodo.forEach(venta -> {
            String mes = venta.getFechaVenta().format(DateTimeFormatter.ofPattern("MMM"));
            ventasPorMes.put(mes, ventasPorMes.getOrDefault(mes, 0) + 1);
            ingresosPorMes.put(mes, ingresosPorMes.getOrDefault(mes, BigDecimal.ZERO).add(venta.getTotal()));
        });

        estadisticas.put("ventasPorMes", ventasPorMes.values());
        estadisticas.put("ingresosPorMes", ingresosPorMes.values());
        estadisticas.put("meses", ventasPorMes.keySet());

        return estadisticas;
    }

    // ===============================
    // API REST - GESTIÓN DE VENTAS
    // ===============================

    @PostMapping("/api/ventas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearVenta(@RequestBody Map<String, Object> ventaData) {
        try {
            return ResponseEntity.badRequest().body(Map.of("success", false, "mensaje", "Este endpoint debe ser refactorizado para la nueva estructura de datos."));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("mensaje", "Error al crear la venta: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/api/ventas/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarVenta(
            @PathVariable Long id,
            @RequestBody Map<String, Object> ventaData) {
        try {
            return ResponseEntity.badRequest().body(Map.of("success", false, "mensaje", "Este endpoint debe ser refactorizado para la nueva estructura de datos."));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("mensaje", "Error al actualizar la venta: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/api/ventas/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarVenta(@PathVariable Integer id) {
        try {
            ventaService.eliminarVenta(id.longValue());

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("mensaje", "Venta eliminada exitosamente");

            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("mensaje", "Error al eliminar la venta: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ===============================
    // API REST - GESTIÓN POR ROLES
    // ===============================

    @GetMapping("/api/usuarios/rol/{rol}")
    @ResponseBody
    public List<AdminUsuarioDTO> obtenerUsuariosPorRol(@PathVariable String rol) {
        return usuarioService.listarTodosLosUsuarios().stream()
                .filter(u -> u.roles().contains("ROLE_" + rol.toUpperCase()))
                .collect(Collectors.toList());
    }

    @PostMapping("/api/usuarios")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearUsuario(@RequestBody Map<String, Object> usuarioData) {
        try {
            Set<Integer> rolesIds = new HashSet<>();
            Object rolesObj = usuarioData.get("roles");
            if (rolesObj instanceof List) {
                for (Object roleObj : (List<?>) rolesObj) {
                    if (roleObj instanceof Number) {
                        rolesIds.add(((Number) roleObj).intValue());
                    }
                }
            } else if (usuarioData.get("rolId") != null) {
                rolesIds.add(Integer.parseInt(usuarioData.get("rolId").toString()));
            }

            UsuarioRegistroDTO dto = new UsuarioRegistroDTO(
                    (String) usuarioData.get("nombre"),
                    (String) usuarioData.get("email"),
                    (String) usuarioData.get("direccion"),
                    null,
                    (String) usuarioData.getOrDefault("tipo_documento", "CC"),
                    (String) usuarioData.getOrDefault("numero_documento", "00000000"),
                    (String) usuarioData.get("telefono"),
                    (String) usuarioData.get("password"),
                    rolesIds
            );

            UsuarioResponseDTO usuarioGuardado = usuarioService.guardarUsuario(dto);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("mensaje", "Usuario creado exitosamente");
            respuesta.put("usuario", usuarioGuardado);

            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("mensaje", "Error al crear el usuario: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/api/usuarios/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarUsuario(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> usuarioData) {
        try {
            List<Integer> rolesIdList = new ArrayList<>();
            Object rolesObj = usuarioData.get("roles");
            if (rolesObj instanceof List) {
                for (Object roleObj : (List<?>) rolesObj) {
                    if (roleObj instanceof Number) {
                        rolesIdList.add(((Number) roleObj).intValue());
                    }
                }
            }

            AdminUsuarioUpdateDTO dto = new AdminUsuarioUpdateDTO(
                    id,
                    (String) usuarioData.get("nombre"),
                    (String) usuarioData.get("email"), // ¡Campo agregado!
                    (Boolean) usuarioData.getOrDefault("activo", true),
                    rolesIdList
            );
            Usuario usuarioActualizado = usuarioService.actualizarUsuarioAdmin(dto);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("mensaje", "Usuario actualizado exitosamente");
            respuesta.put("usuario", usuarioActualizado);

            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("mensaje", "Error al actualizar el usuario: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/api/usuarios/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarUsuario(@PathVariable Integer id) {
        try {
            usuarioService.eliminarUsuario(id);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("mensaje", "Usuario eliminado exitosamente");

            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("mensaje", "Error al eliminar el usuario: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/api/usuarios/{id}/rol")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cambiarRolUsuario(
            @PathVariable Integer id,
            @RequestBody Map<String, String> rolData) {
        try {
            String nuevoRol = rolData.get("rol");
            Usuario usuario = usuarioService.cambiarRolUsuario(id, nuevoRol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("mensaje", "Rol actualizado exitosamente");
            respuesta.put("usuario", usuario);

            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("mensaje", "Error al cambiar el rol: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/api/usuarios/estadisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasUsuarios() {
        try {
            Map<String, Long> estadisticas = usuarioService.obtenerEstadisticasUsuariosPorRol();

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("estadisticas", estadisticas);

            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("mensaje", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}