package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Data
public class ProductoCreateDTO {

    // --- AGREGAR ESTO PARA QUE THYMELEAF NO FALLE ---
    private Integer id;
    // -----------------------------------------------

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    private BigDecimal originalPrice;

    // Nota: Al crear, no pedimos stock (es 0) ni rating.

    private MultipartFile imagenArchivo;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotNull(message = "La categoría es obligatoria")
    private Integer categoriaId;

    private boolean destacado;

    // Helper para evitar nulos en booleanos primitivos si fuera necesario
    public boolean isDestacado() {
        return destacado;
    }
}