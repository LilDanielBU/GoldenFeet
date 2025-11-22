package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.PerfilStatsDTO;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.entity.Venta;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository; // O usa tu Service
import com.GoldenFeet.GoldenFeets.repository.VentaRepository;   // O usa tu Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository; // Lo ideal es usar un Service, pero esto funciona directo

    @Autowired
    private VentaRepository ventaRepository;

    @GetMapping
    public String verPerfil(Model model, Principal principal) {
        // 1. Validar usuario logueado
        if (principal == null) {
            return "redirect:/login";
        }

        // 2. Obtener usuario de la BD (usando el email del login)
        String email = principal.getName();
        Optional<Usuario> usuarioOp = usuarioRepository.findByEmail(email); // Asumo que tienes findByEmail en tu repo de usuario

        if (usuarioOp.isEmpty()) {
            return "redirect:/logout";
        }

        Usuario usuario = usuarioOp.get();

        // 3. Obtener Ventas (Historial de pedidos)
        List<Venta> ventas = ventaRepository.findByCliente_IdUsuario(usuario.getIdUsuario());

        // 4. Calcular Estadísticas
        int totalPedidos = ventas.size();

        // Filtramos las que digan "En Camino" o similar (ajusta el string exacto según tu BD)
        int enCamino = (int) ventas.stream()
                .filter(v -> "En Camino".equalsIgnoreCase(v.getEstado()) || "Enviado".equalsIgnoreCase(v.getEstado()))
                .count();

        // Sumar el total de todas las ventas
        BigDecimal totalGastado = ventas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // NOTA: Como tu entidad Usuario no tiene lista de favoritos, pondré 0 temporalmente.
        int totalFavoritos = 0;

        PerfilStatsDTO stats = new PerfilStatsDTO(totalPedidos, enCamino, totalFavoritos, totalGastado);

        // 5. Pasar datos a la vista
        model.addAttribute("usuario", usuario);
        model.addAttribute("estadisticas", stats);
        model.addAttribute("listaVentas", ventas); // Usaremos 'listaVentas' en el HTML

        return "cliente_perfil"; // Asegúrate que tu HTML se llame cliente_perfil.html o cambia esto
    }
}