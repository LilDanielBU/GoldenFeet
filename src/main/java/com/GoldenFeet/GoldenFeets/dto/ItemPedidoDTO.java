package com.GoldenFeet.GoldenFeets.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoDTO {
    private Long productoId;
    private int cantidad;

    // Validaciones adicionales si las necesitas
    public boolean isValid() {
        return productoId != null && productoId > 0 && cantidad > 0;
    }
}