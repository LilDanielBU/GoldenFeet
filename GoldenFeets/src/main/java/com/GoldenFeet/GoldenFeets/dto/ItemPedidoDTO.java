package com.GoldenFeet.GoldenFeets.dto;

public class ItemPedidoDTO {

    // --- CORRECCIÓN CLAVE ---
    private Integer productoId; // <-- Debe ser Integer

    private int cantidad;

    // Getters y Setters
    public Integer getProductoId() { // <-- Debe ser Integer
        return productoId;
    }
    public void setProductoId(Integer productoId) { // <-- Debe ser Integer
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}