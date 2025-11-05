package com.GoldenFeet.GoldenFeets.dto;

// Puedes usar 'jakarta.validation.constraints' para validaciones
// import jakarta.validation.constraints.Min;
// import jakarta.validation.constraints.NotNull;

public class IngresoDTO {

    // @NotNull
    private Integer productoId;

    // @Min(1)
    private int cantidad;

    private String motivo;

    // Getters y Setters
    public Integer getProductoId() {
        return productoId;
    }
    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
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