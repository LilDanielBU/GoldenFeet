package com.GoldenFeet.GoldenFeets.dto;

import java.math.BigDecimal;
import java.util.List;

// DTO que representa la vista completa del carrito
public record CarritoResponseDTO(
        List<CarritoItemResponseDTO> items,
        BigDecimal total
) {}