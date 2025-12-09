package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.constraints.*; // Importado para las anotaciones de validación
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VarianteDTO {
    private Long id;

    // Validaciones añadidas para Talla
    @NotNull(message = "La talla es obligatoria.")
    @Min(value = 10, message = "La talla debe ser al menos 10.")
    @Max(value = 50, message = "La talla máxima es 50.")
    private Integer talla;

    // Validación añadida para Color
    @NotBlank(message = "El color es obligatorio.")
    private String color;

    // Validación añadida para Stock (puede ser 0)
    @NotNull(message = "El stock inicial es obligatorio.")
    @Min(value = 0, message = "El stock no puede ser negativo.")
    private Integer stock;

    // Campo para la URL completa de la imagen (usado para mostrar en frontend)
    private String imagenUrl;

    // ✔ Campo añadido: Guarda el nombre del archivo físico (usado en el modo Edición
    private String imagenNombre;

    // Para recibir el archivo subido desde el formulario (MultipartFile)
    private MultipartFile imagenArchivo;
}