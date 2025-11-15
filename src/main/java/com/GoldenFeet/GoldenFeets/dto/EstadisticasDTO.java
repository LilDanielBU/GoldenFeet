package com.GoldenFeet.GoldenFeets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasDTO {
    private long totalEntregas;
    private long entregasPendientes;
    private long entregasEnCamino;
    private long entregasCompletadas;
}