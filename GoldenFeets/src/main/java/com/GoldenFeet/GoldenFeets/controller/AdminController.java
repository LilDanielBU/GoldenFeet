package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.AdminUsuarioUpdateDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioRegistroDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioResponseDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioUpdateDTO;
import com.GoldenFeet.GoldenFeets.service.RolService;
import com.GoldenFeet.GoldenFeets.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioService usuarioService;
    private final RolService rolService;

    /**
     * Muestra la página de gestión de usuarios con la lista de todos los usuarios y roles.
     */
    @GetMapping("/usuarios")
    public String gestionarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodosLosUsuarios());
        model.addAttribute("nuevoUsuario", new UsuarioRegistroDTO("", "", "", null, "", "", "", "", null));
        model.addAttribute("rolesTodos", rolService.listarTodosLosRoles()); // Pasa la lista de roles
        return "admin-usuarios";
    }

    /**
     * Es el punto de entrada al panel de admin, redirige a la página principal de gestión.
     */
    @GetMapping("/administrador")
    public String panelAdmin() {
        return "redirect:/admin/usuarios";
    }

    /**
     * Guarda un nuevo usuario creado desde el panel de admin.
     */
    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute("nuevoUsuario") UsuarioRegistroDTO dto, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.guardarUsuario(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario guardado exitosamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    /**
     * Elimina un usuario por su ID.
     */
    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario.");
        }
        return "redirect:/admin/usuarios";
    }
    // ... (tus otros métodos)

    @GetMapping("/usuarios/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Integer id, Model model) {
        // Usamos el DTO de respuesta para obtener los datos actuales
        UsuarioResponseDTO usuarioActual = usuarioService.obtenerPerfil(id);
        model.addAttribute("usuario", usuarioActual);
        model.addAttribute("rolesTodos", rolService.listarTodosLosRoles());
        return "admin-usuario-edit"; // <-- Nueva página HTML
    }

    @PostMapping("/usuarios/actualizar")
// CAMBIO: El parámetro ahora es AdminUsuarioUpdateDTO para que coincida con el servicio
    public String actualizarUsuario(@ModelAttribute("usuario") AdminUsuarioUpdateDTO dto, RedirectAttributes redirectAttributes) {
        try {
            // Ahora los tipos coinciden y la llamada es válida
            usuarioService.actualizarUsuarioAdmin(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}