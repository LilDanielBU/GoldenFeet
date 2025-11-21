package com.GoldenFeet.GoldenFeets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data // Genera getters, setters, equals, hashCode, y toString
@NoArgsConstructor
@AllArgsConstructor // Genera el constructor que usas en ProductoServiceImpl
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
    private String nombreCategoria; // Asegúrate de que coincida con el orden en tu Service

    private Boolean destacado;
    private Float rating;
}