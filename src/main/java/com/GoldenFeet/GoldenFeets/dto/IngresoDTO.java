package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data; // Si usas Lombok, esto ahorra getters/setters

@Data // Agrega getters y setters automáticamente
public class IngresoDTO {

    // --- CORRECCIÓN: Quitamos @NotNull de aquí porque al gestionar stock por variante,
    // el ID del producto general no es estrictamente necesario, ya que la variante lo contiene.
    private Integer productoId;

    // ESTE ES EL CAMPO IMPORTANTE
    @NotNull(message = "El ID de la variante es obligatorio.")
    private Long varianteId;

    @NotNull(message = "La cantidad es obligatoria.")
    @Min(value = 1, message = "La cantidad debe ser mayor o igual a 1.")
    private Integer cantidad;

    @NotBlank(message = "El motivo es obligatorio.")
    @Size(max = 255, message = "El motivo no puede exceder los 255 caracteres.")
    private String motivo;

    // Si no usas Lombok, mantén tus Getters y Setters manuales aquí abajo:
    public Long getVarianteId() { return varianteId; }
    public void setVarianteId(Long varianteId) { this.varianteId = varianteId; }

    public Integer getProductoId() { return productoId; }
    public void setProductoId(Integer productoId) { this.productoId = productoId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}