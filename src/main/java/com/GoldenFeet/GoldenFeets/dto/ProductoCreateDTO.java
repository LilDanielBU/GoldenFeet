package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile; // ⚠️ Importante para subir imágenes
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

    // Nota: En tu servicio actual el stock se inicializa en 0,
    // pero dejamos este campo por si decides cambiar esa lógica después.
    @Min(value = 0, message = "El stock no puede ser negativo.")
    private Integer stock;

    // 💥 CAMBIO IMPORTANTE: Usamos MultipartFile para recibir el archivo real
    // No ponemos validaciones @NotNull aquí para que sea opcional si así lo deseas,
    // o puedes agregar @NotNull(message = "La imagen es obligatoria") si es requerida.
    private MultipartFile imagenArchivo;

    @Size(max = 255, message = "La marca es demasiado larga.")
    private String marca;

    // Se puede dejar nulo si no se especifica
    private Float rating = 0.0f;

    // Por defecto no será destacado
    private boolean destacado = false;

    @NotNull(message = "Debes seleccionar una categoría.")
    private Integer categoriaId; // Usamos Integer para coincidir con la Entidad Categoria
}