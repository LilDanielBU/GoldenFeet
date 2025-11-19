package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.LoginRequestDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioRegistroDTO;
import com.GoldenFeet.GoldenFeets.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Muestra la página de inicio de sesión.
     * @return el nombre de la plantilla "login".
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    /**
     * Muestra el formulario de registro.
     * @param model el modelo para pasar datos a la vista.
     * @return el nombre de la plantilla "register".
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {

        // --- LÍNEA CORREGIDA ---
        // Le pasamos un DTO vacío con los 10 argumentos correctos
        // (nombre, email, direccion, localidad, fecha_nacimiento, tipo_doc, num_doc, telefono, password, roles)
        model.addAttribute("usuarioDto",
                new UsuarioRegistroDTO("", "", "", "", null, "", "", "", "", null) // <-- Ahora tiene 10 argumentos
        );
        // --- FIN DE CORRECCIÓN ---

        return "register";
    }
    /**
     * Procesa los datos enviados desde el formulario de registro.
     * @param usuarioDto el DTO con los datos del formulario.
     * @param bindingResult recoge los errores de validación.
     * @param model el modelo para pasar datos de vuelta a la vista en caso de error.
     * @param redirectAttributes para pasar mensajes entre redirecciones.
     * @return una redirección a /login en caso de éxito, o la vista "register" en caso de error.
     */
    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("usuarioDto") UsuarioRegistroDTO usuarioDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 1. Si hay errores de validación (definidos en el DTO), volvemos al formulario
        if (bindingResult.hasErrors()) {
            // No es necesario volver a añadir el usuarioDto al model, Spring lo hace por nosotros.
            return "register";
        }

        try {
            // 2. Intentamos registrar al usuario a través del servicio
            authService.register(usuarioDto);
            // 3. Si tiene éxito, redirigimos a la página de login con un mensaje
            redirectAttributes.addFlashAttribute("successMessage", "¡Cuenta creada exitosamente! Por favor, inicia sesión.");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            // 4. Si el servicio lanza una excepción (ej: email ya existe), volvemos al formulario con un error
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }
}