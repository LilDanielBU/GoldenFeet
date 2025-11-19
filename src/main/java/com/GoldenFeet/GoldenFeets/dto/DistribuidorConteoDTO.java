package com.GoldenFeet.GoldenFeets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistribuidorConteoDTO {
    private Integer idUsuario;
    private String nombre;
    private long entregasActivasHoy;
    private String localidad; // <-- CORREGIDO: Usamos el nombre 'localidad'
}