package com.GoldenFeet.GoldenFeets.dto;

import java.math.BigDecimal;

// DTO para cada ítem dentro del carrito
public record CarritoItemResponseDTO(
        Integer idProducto,
        String nombreProducto,
        String imagenProducto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}