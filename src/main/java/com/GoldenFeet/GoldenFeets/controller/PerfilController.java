package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.PerfilStatsDTO;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.entity.Venta;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import com.GoldenFeet.GoldenFeets.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Importante si usas encriptación
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VentaRepository ventaRepository;

    // Si estás usando Spring Security con BCrypt, inyéctalo aquí.
    // Si no, puedes quitar esta línea y la lógica de encriptación abajo.
    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    // === TU MÉTODO ORIGINAL (PARA VER EL PERFIL) ===
    @GetMapping
    public String verPerfil(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        Optional<Usuario> usuarioOp = usuarioRepository.findByEmail(email);

        if (usuarioOp.isEmpty()) {
            return "redirect:/logout";
        }

        Usuario usuario = usuarioOp.get();
        List<Venta> ventas = ventaRepository.findByCliente_IdUsuario(usuario.getIdUsuario());

        int totalPedidos = ventas.size();
        int enCamino = (int) ventas.stream()
                .filter(v -> "En Camino".equalsIgnoreCase(v.getEstado()) || "Enviado".equalsIgnoreCase(v.getEstado()))
                .count();

        BigDecimal totalGastado = ventas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalFavoritos = 0;
        PerfilStatsDTO stats = new PerfilStatsDTO(totalPedidos, enCamino, totalFavoritos, totalGastado);

        model.addAttribute("usuario", usuario);
        model.addAttribute("estadisticas", stats);
        model.addAttribute("listaVentas", ventas);

        return "cliente_perfil";
    }

    // === MÉTODOS NUEVOS PARA SOLUCIONAR EL ERROR 404 ===

    // 1. ACTUALIZAR DATOS PERSONALES
    @PostMapping("/actualizar")
    public String actualizarDatos(@ModelAttribute Usuario usuarioForm,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        // Buscamos al usuario REAL de la base de datos para no perder datos (como el password)
        Optional<Usuario> usuarioOp = usuarioRepository.findByEmail(principal.getName());

        if (usuarioOp.isPresent()) {
            Usuario usuarioDB = usuarioOp.get();

            // Actualizamos solo los campos permitidos
            usuarioDB.setNombre(usuarioForm.getNombre());
            usuarioDB.setApellido(usuarioForm.getApellido());
            usuarioDB.setTelefono(usuarioForm.getTelefono());
            usuarioDB.setInformacionAdicional(usuarioForm.getInformacionAdicional());

            usuarioRepository.save(usuarioDB);
            redirectAttributes.addFlashAttribute("successMessage", "¡Datos personales actualizados!");
        }

        return "redirect:/perfil"; // Redirige para evitar reenvío de formulario
    }

    // 2. ACTUALIZAR DIRECCIÓN
    @PostMapping("/actualizar-direccion")
    public String actualizarDireccion(@ModelAttribute Usuario usuarioForm,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        Optional<Usuario> usuarioOp = usuarioRepository.findByEmail(principal.getName());

        if (usuarioOp.isPresent()) {
            Usuario usuarioDB = usuarioOp.get();

            // Mapeo de dirección
            usuarioDB.setDireccion(usuarioForm.getDireccion());
            usuarioDB.setDepartamento(usuarioForm.getDepartamento());
            usuarioDB.setCiudad(usuarioForm.getCiudad());
            usuarioDB.setLocalidad(usuarioForm.getLocalidad());
            usuarioDB.setBarrio(usuarioForm.getBarrio());

            usuarioRepository.save(usuarioDB);
            redirectAttributes.addFlashAttribute("successMessage", "¡Dirección de envío actualizada!");
        }

        return "redirect:/perfil"; // Puedes poner "redirect:/perfil#addresses" si quieres que baje a la sección
    }

    // 3. CAMBIAR CONTRASEÑA
    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam String passwordActual,
                                  @RequestParam String passwordNueva,
                                  @RequestParam String passwordConfirmar,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        Usuario usuarioDB = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuarioDB == null) return "redirect:/logout";

        // Validar que las nuevas coincidan
        if (!passwordNueva.equals(passwordConfirmar)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Las nuevas contraseñas no coinciden.");
            return "redirect:/perfil";
        }

        // Validar contraseña actual (Ajusta según si usas encriptación o texto plano)
        boolean passwordCorrecta = false;

        if (passwordEncoder != null) {
            // Si usas encriptación (Recomendado)
            passwordCorrecta = passwordEncoder.matches(passwordActual, usuarioDB.getPassword());
        } else {
            // Si guardas contraseñas en texto plano (Solo para pruebas)
            passwordCorrecta = passwordActual.equals(usuarioDB.getPassword());
        }

        if (passwordCorrecta) {
            // Guardar nueva
            if (passwordEncoder != null) {
                usuarioDB.setPasswordHash(passwordEncoder.encode(passwordNueva));
            } else {
                usuarioDB.setPasswordHash(passwordNueva);
            }
            usuarioRepository.save(usuarioDB);
            redirectAttributes.addFlashAttribute("successMessage", "Contraseña cambiada exitosamente.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "La contraseña actual es incorrecta.");
        }

        return "redirect:/perfil";
    }
}