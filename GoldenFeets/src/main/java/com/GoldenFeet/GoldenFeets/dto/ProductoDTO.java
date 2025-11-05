package com.GoldenFeet.GoldenFeets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data // Genera getters, setters, equals, hashCode, y toString
@NoArgsConstructor
@AllArgsConstructor // Genera un constructor con todos los argumentos
public class ProductoDTO {

    // El ID del producto ahora es Integer (CORRECTO)
    private Integer id;

    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal originalPrice;
    private int stock;
    private String imagenUrl;
    private String marca;

    // El ID de la categoría ahora es Integer (CORREGIDO)
    private Integer categoriaId;
    private String nombreCategoria; // El nombre del campo en el constructor completo

    private Boolean destacado;
    private Float rating;

    // NOTA: Con @AllArgsConstructor, el constructor con todos los campos ya está generado:
    /*
    public ProductoDTO(Integer id, String nombre, String descripcion, BigDecimal precio,
                       BigDecimal originalPrice, int stock, String imagenUrl, String marca,
                       Integer categoriaId, String nombreCategoria, Boolean destacado, Float rating) {
        // ... (código autogenerado por Lombok)
    }
    */
}