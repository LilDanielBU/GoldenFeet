package com.GoldenFeet.GoldenFeets.dto;

import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventarioMovimientoDTO {

    private Long id;
    private String tipoMovimiento;
    private int cantidad;
    private String motivo;

    // Formateamos la fecha para que se vea bien en el frontend
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fecha;

    // Constructor para mapear fácilmente desde la Entidad
    public InventarioMovimientoDTO(InventarioMovimiento m) {
        this.id = m.getId();
        this.tipoMovimiento = m.getTipoMovimiento();
        this.cantidad = m.getCantidad();
        this.motivo = m.getMotivo();
        this.fecha = m.getFecha();
    }
}