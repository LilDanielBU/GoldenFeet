package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile; // ⚠️ Importante para subir imágenes
import java.math.BigDecimal;

@Data
public class ProductoUpdateDTO {

    private Integer id;

    @NotBlank(message = "El nombre no puede estar vacío.")
    @Size(max = 255, message = "El nombre es demasiado largo.")
    private String nombre;

    @Size(max = 1000, message = "La descripción es demasiado larga.")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero.")
    private BigDecimal precio;

    @DecimalMin(value = "0.0", message = "El precio original no puede ser negativo.")
    private BigDecimal originalPrice;

    @NotNull(message = "El stock es obligatorio.")
    @Min(value = 0, message = "El stock no puede ser negativo.")
    private Integer stock;

    // Campo para mantener la URL de la imagen existente (si no se cambia)
    private String imagenUrl;

    // 💥 CAMBIO IMPORTANTE: Campo para recibir el archivo nuevo si se actualiza la imagen
    private MultipartFile imagenArchivo;

    @Size(max = 255, message = "La marca es demasiado larga.")
    private String marca;

    @NotNull(message = "El rating es obligatorio")
    private Float rating;

    private boolean destacado;

    @NotNull(message = "La categoría es obligatoria.")
    private Integer categoriaId; // Correcto: Integer para coincidir con la entidad
}