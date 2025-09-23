package com.GoldenFeet.GoldenFeets.dto;

import java.math.BigDecimal;

// Se asume que es un 'record', si es una clase, ajusta el tipo del campo y el getter.
public record ProductoDTO(
        Integer id, // <-- CORRECCIÓN: Cambiado de Long a Integer
        String nombre,
        String descripcion,
        BigDecimal precio,
        BigDecimal originalPrice,
        int stock,
        String imagenUrl,
        String nombreCategoria,
        Boolean destacado,
        Float rating
) {
}