package com.GoldenFeet.GoldenFeets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO {

    private Long idCategoria; // <-- CORRECCIÓN AQUÍ

    private String nombre;

    private String descripcion;

    private String imagenUrl;
}