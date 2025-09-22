package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.EstadisticasDistribuidorDTO;
import com.GoldenFeet.GoldenFeets.entity.Entrega;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.service.EntregaService;
import com.GoldenFeet.GoldenFeets.service.PdfService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/distribuidor")
@RequiredArgsConstructor
public class DistribuidorController {

    private final EntregaService entregaService;
    private final PdfService pdfService;

    @GetMapping("/dashboard")
    public String verDashboard(
            @AuthenticationPrincipal Usuario distribuidor,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {

        if (distribuidor == null) {
            return "redirect:/login";
        }

        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;

        List<Entrega> misEntregas = entregaService.buscarConFiltros(
                estado,
                distribuidor.getIdUsuario(),
                inicio,
                fin,
                null
        );

        EstadisticasDistribuidorDTO estadisticas = entregaService.obtenerEstadisticasDistribuidor(distribuidor.getIdUsuario());

        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("entregas", misEntregas);
        model.addAttribute("distribuidor", distribuidor);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("fechaInicioFiltro", fechaInicio);
        model.addAttribute("fechaFinFiltro", fechaFin);

        return "distribuidor/dashboard";
    }

    @PostMapping("/actualizar-estado")
    public String actualizarEstado(@RequestParam("entregaId") Long entregaId,
                                   @RequestParam("nuevoEstado") String nuevoEstado) {
        entregaService.actualizarEstado(entregaId, nuevoEstado);
        return "redirect:/distribuidor/dashboard";
    }

    @PostMapping("/cancelar")
    public String cancelarEntrega(@RequestParam("entregaId") Long entregaId,
                                  @RequestParam("motivo") String motivo) {
        entregaService.cancelarEntrega(entregaId, motivo);
        return "redirect:/distribuidor/dashboard";
    }

    @GetMapping("/exportar-pdf")
    public ResponseEntity<InputStreamResource> exportarEntregasAPdf(
            @AuthenticationPrincipal Usuario distribuidor,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) throws IOException {

        if (distribuidor == null) {
            return ResponseEntity.status(401).build();
        }

        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;
        List<Entrega> entregas = entregaService.buscarConFiltros(estado, distribuidor.getIdUsuario(), inicio, fin, null);

        ByteArrayInputStream bis = pdfService.generarPdfEntregas(entregas);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=reporte-mis-entregas.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}