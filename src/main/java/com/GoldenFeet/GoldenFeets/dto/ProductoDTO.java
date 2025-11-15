package com.GoldenFeet.GoldenFeets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    private Integer id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal originalPrice;
    private int stock;
    private String imagenUrl;
    private String marca;

    // --- CORRECCIÓN ---
    private Integer categoriaId;    // Cambiado de Long a Integer
    private String categoriaNombre;

    private Boolean destacado;
    private Float rating;
}