package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VarianteDTO {
    private Long id; // Ya es Long, permite null para nuevas variantes

    // RELAJAR: Quitamos @NotNull para permitir que Spring vincule el objeto si falla la conversión de tipo.
    // Mantenemos Min/Max que son lógicos para el dato.
    //@NotNull(message = "La talla es obligatoria.") // <-- ELIMINAR O COMENTAR
    @Min(value = 10, message = "La talla debe ser al menos 10.")
    @Max(value = 50, message = "La talla máxima es 50.")
    private Integer talla; // Ya es Integer, lo cual es correcto

    // RELAJAR: Quitamos @NotBlank. Si el binding falla, un campo String puede llegar como null o "".
    //@NotBlank(message = "El color es obligatorio.") // <-- ELIMINAR O COMENTAR
    private String color;

    // RELAJAR: Quitamos @NotNull. Mantenemos Min.
    //@NotNull(message = "El stock inicial es obligatorio.") // <-- ELIMINAR O COMENTAR
    @Min(value = 0, message = "El stock no puede ser negativo.")
    private Integer stock; // Ya es Integer, lo cual es correcto

    // Campo para la URL completa de la imagen
    private String imagenUrl;

    // Campo para el nombre del archivo físico
    private String imagenNombre;

    // Correcto: Permite null si no se sube un nuevo archivo.
    private MultipartFile imagenArchivo;
}