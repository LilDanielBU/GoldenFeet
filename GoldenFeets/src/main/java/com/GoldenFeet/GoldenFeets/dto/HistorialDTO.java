package com.GoldenFeet.GoldenFeets.dto;

import java.time.LocalDateTime; // Importación CRÍTICA: Cambiada de java.util.Date a java.time.LocalDateTime

/**
 * DTO para mostrar el historial de movimientos en el frontend.
 * Usa LocalDateTime para coincidir con la entidad InventarioMovimiento.
 */
public class HistorialDTO {

    // --- CORREGIDO: De Date a LocalDateTime ---
    private LocalDateTime fecha;
    // ----------------------------------------
    private String tipoMovimiento;
    private int cantidad;
    private String motivo;

    // Constructor para facilitar la conversión
    // --- CORREGIDO: El parámetro 'fecha' usa LocalDateTime ---
    public HistorialDTO(LocalDateTime fecha, String tipoMovimiento, int cantidad, String motivo) {
        this.fecha = fecha;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.motivo = motivo;
    }

    // Getters y Setters
    // --- CORREGIDO: Los métodos getFecha y setFecha usan LocalDateTime ---
    public LocalDateTime getFecha() {
        return fecha;
    }
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    // ---------------------------------------------------------------------

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }
    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }
    public int getCantidad() {
        return cantidad;
    }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
    public String getMotivo() {
        return motivo;
    }
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}