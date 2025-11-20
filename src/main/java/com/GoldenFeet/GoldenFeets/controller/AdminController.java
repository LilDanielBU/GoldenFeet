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
import java.util.Arrays;
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

    private final List<String> localidadesBogota = Arrays.asList(
            "Usaquén", "Chapinero", "Santa Fe", "San Cristóbal", "Usme", "Tunjuelito",
            "Bosa", "Kennedy", "Fontibón", "Engativá", "Suba", "Barrios Unidos",
            "Teusaquillo", "Los Mártires", "Antonio Nariño", "Puente Aranda",
            "La Candelaria", "Rafael Uribe Uribe", "Ciudad Bolívar", "Sumapaz"
    );

    @GetMapping("/panel")
    public String mostrarPanel(Model model) {
        // ... (lógica existente) ...
        return "admin-panel";
    }

    @GetMapping("/usuarios")
    public String mostrarUsuarios(Model model) {
        List<Usuario> listaUsuarios = usuarioService.obtenerTodosLosUsuarios();
        model.addAttribute("usuarios", listaUsuarios);
        model.addAttribute("nuevoUsuario", new UsuarioFormDTO());
        List<Rol> rolesTodos = rolService.listarTodosLosRoles();
        model.addAttribute("rolesTodos", rolesTodos);
        model.addAttribute("localidades", localidadesBogota);
        return "admin-usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
        List<Rol> rolesTodos = rolService.listarTodosLosRoles();

        AdminUsuarioUpdateDTO usuarioDto = new AdminUsuarioUpdateDTO();
        usuarioDto.setIdUsuario(usuario.getIdUsuario());
        usuarioDto.setNombre(usuario.getNombre());
        usuarioDto.setEmail(usuario.getEmail());
        usuarioDto.setActivo(usuario.isActivo());
        usuarioDto.setLocalidad(usuario.getLocalidad());
        Set<Integer> rolesIds = usuario.getRoles().stream()
                .map(Rol::getIdRol)
                .collect(Collectors.toSet());
        usuarioDto.setRolesId(rolesIds);

        model.addAttribute("usuario", usuarioDto);
        model.addAttribute("rolesTodos", rolesTodos);
        model.addAttribute("localidades", localidadesBogota);
        return "admin-usuario-edit";
    }

    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuario(AdminUsuarioUpdateDTO usuarioDto, RedirectAttributes redirectAttributes) {
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
        try {
            Set<Integer> rolesIdSet = (usuarioDto.getRolesId() != null) ? new HashSet<>(usuarioDto.getRolesId()) : Set.of();

            // --- CORRECCIÓN DEL CONSTRUCTOR ---
            // Aseguramos que los 10 argumentos coincidan con el DTO (incluyendo los que no están en el form)
            UsuarioRegistroDTO registroDTO = new UsuarioRegistroDTO(
                    usuarioDto.getNombre(),
                    usuarioDto.getEmail(),
                    usuarioDto.getDireccion(),
                    usuarioDto.getLocalidad(),      // <-- Argumento 4 (Localidad)
                    usuarioDto.getFecha_nacimiento(), // <-- Argumento 5 (Fecha Nacimiento)
                    usuarioDto.getTipo_documento(),   // <-- Argumento 6 (Tipo Doc)
                    usuarioDto.getNumero_documento(), // <-- Argumento 7 (Num Doc)
                    usuarioDto.getTelefono(),
                    usuarioDto.getPassword(),
                    rolesIdSet
            );
            // --- FIN DE CORRECCIÓN ---

            usuarioService.guardarUsuario(registroDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario creado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el usuario: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario. Es posible que esté asociado a ventas u otros registros.");
        }
        return "redirect:/admin/usuarios";
    }
}