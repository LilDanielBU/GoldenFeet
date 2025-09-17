package com.GoldenFeet.GoldenFeets.dto;

import java.math.BigDecimal;

public record DetalleVentaDTO(
        Integer idProducto,
        String nombreProducto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}