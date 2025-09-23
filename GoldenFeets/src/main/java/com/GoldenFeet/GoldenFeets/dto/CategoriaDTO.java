package com.GoldenFeet.GoldenFeets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO {

    private Integer idCategoria; // <-- Changed from Long to Integer

    private String nombre;

    private String descripcion;

    private String imagenUrl;
}