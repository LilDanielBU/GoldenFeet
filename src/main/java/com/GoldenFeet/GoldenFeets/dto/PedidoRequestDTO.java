package com.GoldenFeet.GoldenFeets.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class PedidoRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio.")
    private Integer clienteId;

    @NotNull(message = "El pedido debe contener al menos un artículo.")
    private List<ItemPedidoDTO> items;

    // --- CAMPOS NECESARIOS PARA COMPLETAR LA ENTIDAD Venta ---

    // Datos de Envío
    private String direccionEnvio;
    private String ciudadEnvio;
    private String localidad; // Coincidente con el campo añadido en Venta

    // Datos de Pago
    private String metodoPago;
    private String idTransaccion;

    // Si necesitas campos adicionales (ej. código postal) añádelos aquí.
}