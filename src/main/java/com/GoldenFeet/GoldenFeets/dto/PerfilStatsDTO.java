package com.GoldenFeet.GoldenFeets.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PerfilStatsDTO {
    private int totalPedidos;
    private int pedidosEnCamino;
    private int totalFavoritos;
    private BigDecimal totalGastado;
}