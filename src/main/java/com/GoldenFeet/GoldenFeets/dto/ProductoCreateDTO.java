package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

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



    private MultipartFile imagenArchivo;

    @Size(max = 255, message = "La marca es demasiado larga.")
    private String marca;



    // Por defecto no será destacado
    private boolean destacado = false;

    @NotNull(message = "Debes seleccionar una categoría.")
    private Integer categoriaId;
}