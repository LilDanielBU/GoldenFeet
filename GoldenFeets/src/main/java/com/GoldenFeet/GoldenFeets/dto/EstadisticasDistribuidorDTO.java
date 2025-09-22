package com.GoldenFeet.GoldenFeets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasDistribuidorDTO {
    private long totalAsignadas;
    private long enCamino;
    private long completadasHoy;
}