package com.GoldenFeet.GoldenFeets.dto;

// Importaciones necesarias para la validación
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank; // <-- ¡NUEVO!
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class IngresoDTO {

    // El ID del producto es obligatorio para saber a qué producto agregar/restar stock
    @NotNull(message = "El ID del producto es obligatorio.")
    private Integer productoId;

    // La cantidad debe ser obligatoriamente 1 o mayor
    @NotNull(message = "La cantidad es obligatoria.") // Asegura que no sea null
    @Min(value = 1, message = "La cantidad debe ser mayor o igual a 1.")
    private Integer cantidad;

    // --- CORRECCIÓN CLAVE ---
    // El motivo debe ser obligatorio para el registro de SALIDA
    @NotBlank(message = "El motivo es obligatorio para registrar movimientos de stock.")
    @Size(max = 255, message = "El motivo no puede exceder los 255 caracteres.")
    private String motivo;

    // Getters y Setters
    public @NotNull(message = "El ID del producto es obligatorio.") Integer getProductoId() {
        return productoId;
    }
    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}