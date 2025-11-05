package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class ProductoUpdateDTO {
    private Integer id;
    @NotBlank(message = "El nombre no puede estar vacío.")
    @Size(max = 255)
    private String nombre;

    @Size(max = 1000)
    private String descripcion;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal precio;

    @DecimalMin(value = "0.0")
    private BigDecimal originalPrice;

    @NotNull(message = "El stock es obligatorio.")
    @Min(value = 0)
    private Integer stock;

    private MultipartFile imagenArchivo;

    @Size(max = 255)
    private String marca;



    private boolean destacado;

    // --- CORRECCIÓN ---
    @NotNull(message = "La categoría es obligatoria.")
    private Integer categoriaId; // Cambiado de Long a Integer
}