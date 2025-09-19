package com.GoldenFeet.GoldenFeets.dto;

import java.math.BigDecimal;

// Usamos 'record' que es una forma moderna y concisa de crear DTOs en Java
public record ProductoDTO(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        BigDecimal originalPrice, // <-- Campo que faltaba
        int stock,
        String imagenUrl,
        String nombreCategoria,
        boolean destacado, // <-- Campo que faltaba (en lugar de 'descuento')
        double rating      // <-- Campo que faltaba
) {}