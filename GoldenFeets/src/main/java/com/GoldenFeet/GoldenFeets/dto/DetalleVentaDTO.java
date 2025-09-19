package com.GoldenFeet.GoldenFeets.dto;

import java.math.BigDecimal;

// CORRECCIÓN: idProducto ahora es Long
public record DetalleVentaDTO(
        Long idProducto,
        String nombreProducto,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}