package com.GoldenFeet.GoldenFeets.dto;

import java.math.BigDecimal;

public record CarritoItemDTO(
        ProductoDTO producto,
        int cantidad,
        BigDecimal precioTotal
) {}