package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.entity.Entrega;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.service.EntregaService;
import com.GoldenFeet.GoldenFeets.service.PdfService;
import com.GoldenFeet.GoldenFeets.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/gerente-entregas")
@RequiredArgsConstructor
public class GerenteEntregasController {

    private final EntregaService entregaService;
    private final UsuarioService usuarioService;
    private final PdfService pdfService; // Inyección del nuevo servicio de PDF

    @GetMapping("/dashboard")
    public String verDashboard(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "distribuidorId", required = false) Integer distribuidorId,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(value = "clienteEmail", required = false) String clienteEmail,
            Model model) {

        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;

        List<Entrega> listaDeEntregas = entregaService.buscarConFiltros(estado, distribuidorId, inicio, fin, clienteEmail);
        model.addAttribute("entregas", listaDeEntregas);

        model.addAttribute("estadisticas", entregaService.obtenerEstadisticas());
        model.addAttribute("distribuidores", usuarioService.findByRol("ROLE_DISTRIBUIDOR"));

        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("distribuidorIdFiltro", distribuidorId);
        model.addAttribute("fechaInicioFiltro", fechaInicio);
        model.addAttribute("fechaFinFiltro", fechaFin);
        model.addAttribute("clienteEmailFiltro", clienteEmail);

        return "gerente-entregas/dashboard";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalleEntrega(@PathVariable("id") Long id, Model model) {
        Entrega entrega = entregaService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de Entrega no válido: " + id));
        model.addAttribute("entrega", entrega);
        return "gerente-entregas/detalle-entrega";
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

    @PostMapping("/desasignar")
    public String desasignarDistribuidor(@RequestParam("entregaId") Long entregaId) {
        entregaService.desasignarDistribuidor(entregaId);
        return "redirect:/gerente-entregas/dashboard";
    }

    // --- MÉTODO NUEVO PARA EXPORTAR A PDF ---
    @GetMapping("/exportar-pdf")
    public ResponseEntity<InputStreamResource> exportarEntregasAPdf(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "distribuidorId", required = false) Integer distribuidorId,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(value = "clienteEmail", required = false) String clienteEmail) throws IOException {

        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;
        List<Entrega> entregas = entregaService.buscarConFiltros(estado, distribuidorId, inicio, fin, clienteEmail);

        ByteArrayInputStream bis = pdfService.generarPdfEntregas(entregas);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=reporte-entregas.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}