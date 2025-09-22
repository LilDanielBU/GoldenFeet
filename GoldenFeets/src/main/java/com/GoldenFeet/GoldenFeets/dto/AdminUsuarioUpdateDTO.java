package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO para actualización de usuarios desde el panel de administración
 */
public record AdminUsuarioUpdateDTO(
        @NotNull(message = "El ID del usuario es obligatorio")
        Integer idUsuario,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        String email,
        String password,

        @NotNull(message = "El estado activo es obligatorio")
        Boolean activo,

        List<Integer> rolesId
) {

    // Constructor adicional corregido para incluir el nuevo campo 'password'
    public AdminUsuarioUpdateDTO(Integer idUsuario, String nombre, String email, Boolean activo, List<Integer> rolesId) {
        this(idUsuario, nombre, email, null, activo, rolesId);
    }
}