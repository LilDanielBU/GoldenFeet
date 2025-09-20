package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.entity.Entrega;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.service.EntregaService;
import com.GoldenFeet.GoldenFeets.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/gerente-entregas")
public class GerenteEntregasController {

    @Autowired
    private EntregaService entregaService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model) {
        // Buscamos todas las entregas para la tabla principal
        model.addAttribute("entregas", entregaService.findAll());

        // --- LÍNEA CORREGIDA ---
        // Buscamos todos los distribuidores y los enviamos a la vista para el modal.
        List<Usuario> distribuidores = usuarioService.findByRol("ROLE_DISTRIBUIDOR");
        model.addAttribute("distribuidores", distribuidores);

        return "gerente-entregas/dashboard";
    }

    @PostMapping("/asignar")
    public String procesarAsignacion(@RequestParam("entregaId") Long entregaId,
                                     @RequestParam("distribuidorId") Integer distribuidorId) {
        entregaService.asignarDistribuidor(entregaId, distribuidorId);
        return "redirect:/gerente-entregas/dashboard";
    }

    @PostMapping("/cancelar")
    public String procesarCancelacion(@RequestParam("entregaId") Long entregaId,
                                      @RequestParam("motivo") String motivo) {
        entregaService.cancelarEntrega(entregaId, motivo);
        return "redirect:/gerente-entregas/dashboard";
    }

    // Este método es para el botón de desasignar
    @GetMapping("/desasignar/{entregaId}")
    public String desasignarDistribuidor(@PathVariable("entregaId") Long entregaId) {
        entregaService.desasignarDistribuidor(entregaId);
        return "redirect:/gerente-entregas/dashboard";
    }
}