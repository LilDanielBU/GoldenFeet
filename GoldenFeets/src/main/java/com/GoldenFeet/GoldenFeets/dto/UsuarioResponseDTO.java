package com.GoldenFeet.GoldenFeets.dto;

import java.util.Set;

public record UsuarioResponseDTO(
        Integer idUsuario,
        String nombre,
        String email,
        String telefono,
        String direccion,
        Set<String> roles // Devolvemos solo los nombres de los roles
) {}