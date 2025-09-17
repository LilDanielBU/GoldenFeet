package com.GoldenFeet.GoldenFeets.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VentaResponseDTO(
        Integer idVenta,
        LocalDate fechaVenta,
        BigDecimal total,
        String estado,
        String emailCliente, // Campo aplanado del Usuario
        List<DetalleVentaDTO> detalles // Lista anidada de productos
) {}