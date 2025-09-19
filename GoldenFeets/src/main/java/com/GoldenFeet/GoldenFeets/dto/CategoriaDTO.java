package com.GoldenFeet.GoldenFeets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO {

    // Corresponde a categoria.getIdCategoria()
    private Integer idCategoria;

    // Corresponde a categoria.getNombre()
    private String nombre;

    // Corresponde a categoria.getDescripcion()
    private String descripcion;

    private String imagenUrl;
}