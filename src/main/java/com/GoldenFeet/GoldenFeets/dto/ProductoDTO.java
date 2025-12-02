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

    // Campos de Categoría
    private Integer categoriaId;
    private String nombreCategoria;

    private Boolean destacado;
    private Float rating;

    // === CAMPOS CLAVE PARA VARIANTES ===
    private Integer talla;
    private String color;
}