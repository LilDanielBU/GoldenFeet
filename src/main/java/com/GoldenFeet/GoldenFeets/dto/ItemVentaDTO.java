package com.GoldenFeet.GoldenFeets.dto;

// Archivo ItemVentaDTO.java (Estructura necesaria)
public record ItemVentaDTO(
        Long productoId,
        Integer cantidad,
        String talla,     // NUEVO
        String color      // NUEVO
) {
}