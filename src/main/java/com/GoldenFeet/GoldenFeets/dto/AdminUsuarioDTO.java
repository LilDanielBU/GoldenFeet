package com.GoldenFeet.GoldenFeets.dto;

import java.util.Set;

// DTO para mostrar usuarios en la tabla de admin
public record AdminUsuarioDTO(
        Integer idUsuario,
        String nombre,
        String email,
        boolean activo,
        Set<String> roles
) {}