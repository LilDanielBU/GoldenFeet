package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.Valid; // Importado para validar la lista
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    private String imagenUrl;
    private MultipartFile imagenArchivo;

    @NotBlank(message = "La marca es obligatoria.")
    private String marca;
    private Float rating;
    private Boolean destacado;

    @NotNull(message = "La categoría es obligatoria.")
    private Integer categoriaId;

    // Se mantiene @Valid para asegurar que se apliquen las validaciones definidas en VarianteDTO
    @Valid
    private List<VarianteDTO> variantes = new ArrayList<>();

    public boolean getDestacado() {
        return destacado != null && destacado;
    }
}