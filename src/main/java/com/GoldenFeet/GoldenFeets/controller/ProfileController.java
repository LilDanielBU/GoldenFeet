package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UsuarioService usuarioService;

    // Este método se encarga de MOSTRAR la página del perfil
    @GetMapping("/perfil")
    public String verPerfil(Model model) {
        // Obtenemos el email del usuario que ha iniciado sesión
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Buscamos al usuario en la base de datos
        Usuario usuario = usuarioService.buscarPorEmail(userEmail);

        // Si no se encuentra, redirigimos al inicio (esto es un caso raro)
        if (usuario == null) {
            return "redirect:/";
        }

        // Añadimos el objeto 'usuario' al modelo para que la vista HTML lo use
        model.addAttribute("usuario", usuario);

        // Devolvemos el nombre del archivo HTML que vamos a crear
        return "perfil";
    }

    // Este método se encarga de PROCESAR la actualización de datos
    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@ModelAttribute Usuario usuarioActualizado, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Usuario usuarioExistente = usuarioService.buscarPorEmail(userEmail);

        if (usuarioExistente != null) {
            // Actualizamos solo los campos que permitimos cambiar
            usuarioExistente.setNombre(usuarioActualizado.getNombre());
            usuarioExistente.setApellido(usuarioActualizado.getApellido());
            usuarioExistente.setDireccion(usuarioActualizado.getDireccion());
            usuarioExistente.setTelefono(usuarioActualizado.getTelefono());
            // Y los demás campos que añadiste...
            usuarioExistente.setDepartamento(usuarioActualizado.getDepartamento());
            usuarioExistente.setCiudad(usuarioActualizado.getCiudad());
            usuarioExistente.setLocalidad(usuarioActualizado.getLocalidad());
            usuarioExistente.setBarrio(usuarioActualizado.getBarrio());

            usuarioService.guardarUsuario(usuarioExistente); // Asumimos que tienes un método 'guardar' en tu servicio

            redirectAttributes.addFlashAttribute("successMessage", "¡Perfil actualizado con éxito!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el perfil.");
        }

        return "redirect:/perfil";
    }
}