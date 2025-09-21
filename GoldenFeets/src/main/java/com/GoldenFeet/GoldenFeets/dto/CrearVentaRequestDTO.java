package com.GoldenFeet.GoldenFeets.dto;

import java.util.List;

// Este 'record' ahora incluye toda la información necesaria para crear el pedido.
public record CrearVentaRequestDTO(

        List<ItemVentaDTO> items,

        // --- NUEVOS CAMPOS ---
        // Datos de envío
        String nombre,
        String apellido,
        String direccion,
        String ciudad,
        String departamento,

        // Datos de pago
        String metodoPago
) {}