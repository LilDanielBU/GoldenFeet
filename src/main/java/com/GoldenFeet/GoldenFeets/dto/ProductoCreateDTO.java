package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Data
public class ProductoCreateDTO {

    private Integer id; // Necesario para evitar error en el form único

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    private BigDecimal originalPrice;

    // --- NUEVOS CAMPOS ---
    @NotNull(message = "La talla es obligatoria")
    private Integer talla;

    @NotBlank(message = "El color es obligatorio")
    private String color;
    // ---------------------

    private MultipartFile imagenArchivo;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotNull(message = "La categoría es obligatoria")
    private Integer categoriaId;

    private boolean destacado;



}