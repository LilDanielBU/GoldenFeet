package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Data
public class ProductoUpdateDTO {

    private Integer id;

    @NotBlank(message = "El nombre no puede estar vacío.")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero.")
    private BigDecimal precio;

    @DecimalMin(value = "0.0", message = "El precio original no puede ser negativo.")
    private BigDecimal originalPrice;

    private Integer stock; // Opcional

    // --- NUEVOS CAMPOS ---
    @NotNull(message = "La talla es obligatoria")
    private Integer talla;

    @NotBlank(message = "El color es obligatorio")
    private String color;
    // ---------------------

    private String imagenUrl;
    private MultipartFile imagenArchivo;

    private String marca;
    private Float rating;
    private Boolean destacado;

    @NotNull(message = "La categoría es obligatoria.")
    private Integer categoriaId;

    public boolean getDestacado() {
        return destacado != null && destacado;
    }
}