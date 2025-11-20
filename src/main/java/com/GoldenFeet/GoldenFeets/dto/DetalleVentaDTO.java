package com.GoldenFeet.GoldenFeets.dto;

import java.math.BigDecimal;

public record DetalleVentaDTO(
        Long productoId,
        String nombreProducto,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}