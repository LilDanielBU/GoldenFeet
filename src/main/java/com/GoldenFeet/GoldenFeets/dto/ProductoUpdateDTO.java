package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
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

    // --- CORRECCIÓN: Eliminadas las validaciones de Stock ---
    // Al quitar @NotNull, si el formulario no envía stock, llegará como null y no pasará nada.
    private Integer stock;

    // Campo para mantener la URL de la imagen existente (si no se cambia)
    private String imagenUrl;

    // Campo para recibir el archivo nuevo si se actualiza la imagen
    private MultipartFile imagenArchivo;

    @Size(max = 255, message = "La marca es demasiado larga.")
    private String marca;

    // --- CORRECCIÓN: Eliminada la validación de Rating ---
    // Igual que el stock, ahora es opcional en la actualización.
    private Float rating;

    // fix: cambiamos 'boolean' (primitivo) a 'Boolean' (objeto) para evitar problemas con nulos en formularios,
    // aunque 'boolean' primitivo suele funcionar, el wrapper es más seguro en DTOs.
    private Boolean destacado;

    @NotNull(message = "La categoría es obligatoria.")
    private Integer categoriaId;

    // Método helper por si necesitas evitar NullPointerException al leer el destacado
    public boolean getDestacado() {
        return destacado != null && destacado;
    }
}