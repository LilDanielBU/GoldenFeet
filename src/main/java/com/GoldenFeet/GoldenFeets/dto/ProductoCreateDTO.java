package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoCreateDTO {

    @NotBlank(message = "El nombre no puede estar vacío.")
    @Size(max = 255, message = "El nombre es demasiado largo.")
    private String nombre;

    @Size(max = 1000, message = "La descripción es demasiado larga.")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero.")
    private BigDecimal precio;

    // Este campo es opcional
    @DecimalMin(value = "0.0", message = "El precio original no puede ser negativo.")
    private BigDecimal originalPrice;

    @NotNull(message = "El stock es obligatorio.")
    @Min(value = 0, message = "El stock no puede ser negativo.")
    private Integer stock;

    @Size(max = 255, message = "La URL de la imagen es demasiado larga.")
    private String imagenUrl;

    @Size(max = 255, message = "La marca es demasiado larga.")
    private String marca;

    // Se puede dejar nulo si no se especifica
    private Float rating = 0.0f;

    // Por defecto no será destacado
    private boolean destacado = false;

    @NotNull(message = "Debes seleccionar una categoría.")
    private Long categoriaId;
}