package com.GoldenFeet.GoldenFeets.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoDTO {

    /**
     * El ID del producto.
     * Se cambió de Long a Integer para que coincida con el tipo de dato
     * del ID en la entidad Producto.
     */
    private Integer productoId; // <--- ESTE ES EL CAMBIO

    private int cantidad;

    // Validaciones adicionales si las necesitas
    public boolean isValid() {
        return productoId != null && productoId > 0 && cantidad > 0;
    }
}