package com.GoldenFeet.GoldenFeets.dto;

import lombok.Data;
import java.util.List;

@Data
public class CrearVentaRequestDTO {

    private List<ItemVentaDTO> items;

    // --- DATOS DE ENVÍO ---
    private String nombre;
    private String apellido;
    private String direccion;
    private String ciudad;
    private String departamento;
    private String localidad; // <-- CAMPO AÑADIDO

    // Datos de pago
    private String metodoPago;
}