package com.GoldenFeet.GoldenFeets.dto;

import lombok.Data;
import java.util.List;

// Representa el JSON que enviará el frontend
@Data
public class PedidoRequestDTO {
    private List<ItemPedidoDTO> items;
    // Aquí puedes añadir los datos de envío (nombre, dirección, etc.)
    // private String nombreCliente;
    // private String direccionEnvio;
}