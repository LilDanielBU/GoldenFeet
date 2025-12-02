package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.PerfilStatsDTO;
import com.GoldenFeet.GoldenFeets.entity.Entrega;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.entity.Venta;
import com.GoldenFeet.GoldenFeets.repository.EntregaRepository;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import com.GoldenFeet.GoldenFeets.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private EntregaRepository entregaRepository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    // === CLASE DTO INTERNA PARA LA VISTA ===
    // Sirve para enviar la venta + el estado calculado (En Camino/Completado)
    public static class PedidoDisplayDTO {
        private Venta venta;
        private String estadoVisual;

        public PedidoDisplayDTO(Venta venta, String estadoVisual) {
            this.venta = venta;
            this.estadoVisual = estadoVisual;
        }

        public Venta getVenta() { return venta; }
        public String getEstadoVisual() { return estadoVisual; }
    }

    // === VER PERFIL ===
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

        // 1. Obtener todas las ventas del usuario
        List<Venta> ventas = ventaRepository.findByCliente_IdUsuario(usuario.getIdUsuario());

        // 2. Procesar cada venta para determinar su estado real según la Entrega
        List<PedidoDisplayDTO> listaPedidosVisual = new ArrayList<>();

        int enCaminoCount = 0;
        BigDecimal totalGastado = BigDecimal.ZERO;

        for (Venta v : ventas) {
            // Calcular total gastado
            if (v.getTotal() != null) {
                totalGastado = totalGastado.add(v.getTotal());
            }

            // Buscar si existe una entrega para esta venta
            Optional<Entrega> entregaOp = entregaRepository.findByVenta_IdVenta(v.getIdVenta());

            String estadoParaMostrar = "Procesando"; // Estado por defecto

            if (entregaOp.isPresent()) {
                Entrega entrega = entregaOp.get();
                String estadoEntrega = entrega.getEstado();

                // LÓGICA DE ESTADOS
                if ("ASIGNADO".equalsIgnoreCase(estadoEntrega) ||
                        "EN CAMINO".equalsIgnoreCase(estadoEntrega) ||
                        "RECOGIDO".equalsIgnoreCase(estadoEntrega)) {

                    estadoParaMostrar = "En Camino";
                    enCaminoCount++;

                } else if ("ENTREGADO".equalsIgnoreCase(estadoEntrega) ||
                        "COMPLETADO".equalsIgnoreCase(estadoEntrega)) {

                    estadoParaMostrar = "Completado";

                } else if ("CANCELADO".equalsIgnoreCase(estadoEntrega)) {
                    estadoParaMostrar = "Cancelado";
                } else {
                    estadoParaMostrar = estadoEntrega; // Otros estados
                }
            } else {
                // Si no hay registro en entregas, usamos el estado base de la venta
                estadoParaMostrar = v.getEstado();
            }

            listaPedidosVisual.add(new PedidoDisplayDTO(v, estadoParaMostrar));
        }

        int totalPedidos = ventas.size();
        int totalFavoritos = 0; // Ajustar si tienes lógica de favoritos

        PerfilStatsDTO stats = new PerfilStatsDTO(totalPedidos, enCaminoCount, totalFavoritos, totalGastado);

        model.addAttribute("usuario", usuario);
        model.addAttribute("estadisticas", stats);
        // IMPORTANTE: Enviamos la lista procesada (DTOs)
        model.addAttribute("listaVentas", listaPedidosVisual);

        return "cliente_perfil";
    }

    // === ACTUALIZAR DATOS PERSONALES ===
    @PostMapping("/actualizar")
    public String actualizarDatos(@ModelAttribute Usuario usuarioForm,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        Optional<Usuario> usuarioOp = usuarioRepository.findByEmail(principal.getName());

        if (usuarioOp.isPresent()) {
            Usuario usuarioDB = usuarioOp.get();
            usuarioDB.setNombre(usuarioForm.getNombre());
            usuarioDB.setApellido(usuarioForm.getApellido());
            usuarioDB.setTelefono(usuarioForm.getTelefono());
            usuarioDB.setInformacionAdicional(usuarioForm.getInformacionAdicional());

            usuarioRepository.save(usuarioDB);
            redirectAttributes.addFlashAttribute("successMessage", "¡Datos personales actualizados!");
        }
        return "redirect:/perfil";
    }

    // === ACTUALIZAR DIRECCIÓN ===
    @PostMapping("/actualizar-direccion")
    public String actualizarDireccion(@ModelAttribute Usuario usuarioForm,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        Optional<Usuario> usuarioOp = usuarioRepository.findByEmail(principal.getName());

        if (usuarioOp.isPresent()) {
            Usuario usuarioDB = usuarioOp.get();
            usuarioDB.setDireccion(usuarioForm.getDireccion());
            usuarioDB.setDepartamento(usuarioForm.getDepartamento());
            usuarioDB.setCiudad(usuarioForm.getCiudad());
            usuarioDB.setLocalidad(usuarioForm.getLocalidad());
            usuarioDB.setBarrio(usuarioForm.getBarrio());

            usuarioRepository.save(usuarioDB);
            redirectAttributes.addFlashAttribute("successMessage", "¡Dirección de envío actualizada!");
        }
        return "redirect:/perfil";
    }

    // === CAMBIAR CONTRASEÑA ===
    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam String passwordActual,
                                  @RequestParam String passwordNueva,
                                  @RequestParam String passwordConfirmar,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        Usuario usuarioDB = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuarioDB == null) return "redirect:/logout";

        if (!passwordNueva.equals(passwordConfirmar)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Las nuevas contraseñas no coinciden.");
            return "redirect:/perfil";
        }

        boolean passwordCorrecta = false;
        if (passwordEncoder != null) {
            passwordCorrecta = passwordEncoder.matches(passwordActual, usuarioDB.getPassword());
        } else {
            passwordCorrecta = passwordActual.equals(usuarioDB.getPassword());
        }

        if (passwordCorrecta) {
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