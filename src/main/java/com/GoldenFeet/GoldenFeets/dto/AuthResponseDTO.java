package com.GoldenFeet.GoldenFeets.dto;

public record AuthResponseDTO(
        String token,
        String email
) {}