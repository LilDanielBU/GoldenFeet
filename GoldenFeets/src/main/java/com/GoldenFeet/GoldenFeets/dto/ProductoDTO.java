package com.GoldenFeet.GoldenFeets.dto;

import java.math.BigDecimal;

public record ProductoDTO(
        Integer idProducto,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        String imagen,
        String nombreCategoria // Campo aplanado
) {}