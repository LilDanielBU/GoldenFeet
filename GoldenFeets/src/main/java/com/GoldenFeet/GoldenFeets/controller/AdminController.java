package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.AdminUsuarioUpdateDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioFormDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioRegistroDTO;
import com.GoldenFeet.GoldenFeets.dto.VentaResponseDTO;
import com.GoldenFeet.GoldenFeets.entity.Rol;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.service.RolService;
import com.GoldenFeet.GoldenFeets.service.UsuarioService;
import com.GoldenFeet.GoldenFeets.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    @GetMapping("/panel")
    public String mostrarPanel(Model model) {
        // ... (tu método existente, sin cambios)
        List<VentaResponseDTO> todasLasVentas = ventaService.findAllVentas();
        List<Usuario> todosLosUsuarios = usuarioService.obtenerTodosLosUsuarios();
        List<Usuario> clientes = todosLosUsuarios.stream()
                .filter(usuario -> usuario.getRoles().stream()
                        .anyMatch(rol -> rol.getNombre().equals("ROLE_CLIENTE")))
                .collect(Collectors.toList());
        List<Usuario> empleados = todosLosUsuarios.stream()
                .filter(usuario -> usuario.getRoles().stream()
                        .anyMatch(rol -> rol.getNombre().equals("ROLE_EMPLEADO") || rol.getNombre().equals("ROLE_DISTRIBUIDOR")))
                .collect(Collectors.toList());
        BigDecimal totalIngresos = todasLasVentas.stream()
                .map(VentaResponseDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalVentas = todasLasVentas.size();
        int totalClientes = clientes.size();
        int totalEmpleados = empleados.size();
        List<VentaResponseDTO> ventasRecientes = todasLasVentas.stream()
                .sorted(Comparator.comparing(VentaResponseDTO::getFechaVenta).reversed())
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalEmpleados", totalEmpleados);
        model.addAttribute("ultimasVentas", ventasRecientes);
        return "admin-panel";
    }

    @GetMapping("/usuarios")
    public String mostrarUsuarios(Model model) {
        // ... (tu método existente, sin cambios)
        List<Usuario> listaUsuarios = usuarioService.obtenerTodosLosUsuarios();
        model.addAttribute("usuarios", listaUsuarios);
        model.addAttribute("nuevoUsuario", new UsuarioFormDTO());
        List<Rol> rolesTodos = rolService.listarTodosLosRoles();
        model.addAttribute("rolesTodos", rolesTodos);
        return "admin-usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        // ... (tu método existente, sin cambios)
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
        List<Rol> rolesTodos = rolService.listarTodosLosRoles();

        AdminUsuarioUpdateDTO usuarioDto = new AdminUsuarioUpdateDTO();
        usuarioDto.setIdUsuario(usuario.getIdUsuario());
        usuarioDto.setNombre(usuario.getNombre());
        usuarioDto.setEmail(usuario.getEmail());
        usuarioDto.setActivo(usuario.isActivo());
        Set<Integer> rolesIds = usuario.getRoles().stream()
                .map(Rol::getIdRol)
                .collect(Collectors.toSet());
        usuarioDto.setRolesId(rolesIds);

        model.addAttribute("usuario", usuarioDto);
        model.addAttribute("rolesTodos", rolesTodos);
        return "admin-usuario-edit";
    }

    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuario(AdminUsuarioUpdateDTO usuarioDto, RedirectAttributes redirectAttributes) {
        // ... (tu método existente, sin cambios)
        try {
            usuarioService.actualizarUsuarioAdmin(usuarioDto);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
    @PostMapping("/usuarios/guardar")
    public String guardarNuevoUsuario(@ModelAttribute("nuevoUsuario") UsuarioFormDTO usuarioDto, RedirectAttributes redirectAttributes) {
        // ... (tu método existente, sin cambios)
        try {
            Set<Integer> rolesIdSet = (usuarioDto.getRolesId() != null) ? new HashSet<>(usuarioDto.getRolesId()) : Set.of();

            UsuarioRegistroDTO registroDTO = new UsuarioRegistroDTO(
                    usuarioDto.getNombre(),
                    usuarioDto.getEmail(),
                    usuarioDto.getDireccion(),
                    null,
                    "N/A",
                    "N/A",
                    usuarioDto.getTelefono(),
                    usuarioDto.getPassword(),
                    rolesIdSet
            );

            usuarioService.guardarUsuario(registroDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario creado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el usuario: " + e.getMessage());
        }

        return "redirect:/admin/usuarios";
    }

    // --- MÉTODO NUEVO PARA ELIMINAR ---
    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            // Captura cualquier excepción, incluyendo las de clave foránea, y muestra un mensaje amigable
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario. Es posible que esté asociado a ventas u otros registros.");
        }
        return "redirect:/admin/usuarios";
    }
}