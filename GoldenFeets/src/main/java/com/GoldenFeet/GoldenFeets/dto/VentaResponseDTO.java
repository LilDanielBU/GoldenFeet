package com.GoldenFeet.GoldenFeets.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// CORRECCIÓN: idVenta ahora es Long
public record VentaResponseDTO(
        Long idVenta,
        LocalDate fechaVenta,
        BigDecimal total,
        String estado,
        String clienteEmail,
        List<DetalleVentaDTO> detalles
) {}