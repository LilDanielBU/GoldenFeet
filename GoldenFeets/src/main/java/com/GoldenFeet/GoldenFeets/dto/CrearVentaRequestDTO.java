package com.GoldenFeet.GoldenFeets.dto;

import java.util.List;

public record CrearVentaRequestDTO(
        Integer idCliente,
        List<ItemVentaDTO> items
) {}