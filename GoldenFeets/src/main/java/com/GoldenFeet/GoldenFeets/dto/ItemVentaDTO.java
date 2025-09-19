package com.GoldenFeet.GoldenFeets.dto;

// CORRECCIÓN: idProducto ahora es Long
public record ItemVentaDTO(
        Long idProducto,
        int cantidad
) {}