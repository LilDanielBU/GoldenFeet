package com.GoldenFeet.GoldenFeets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder; // NUEVA IMPORTACIÓN
import lombok.Data;
import lombok.NoArgsConstructor; // NUEVA IMPORTACIÓN

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder // Útil si mapeas muchos campos
@NoArgsConstructor // Necesario si usas Builder o si Spring o librerías lo requieren (ej: Jackson)
@AllArgsConstructor
public class ProductoDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal originalPrice;
    private int stockTotal;
    // CRÍTICO: Este campo debe contener el NOMBRE DEL ARCHIVO (ej: "guayos_1.jpg")
    private String imagenUrl;
    private String marca;
    private Integer categoriaId;
    private String nombreCategoria;
    private Boolean destacado;
    private Float rating;
    private List<VarianteDTO> variantes;
}