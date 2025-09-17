package com.GoldenFeet.GoldenFeets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

public record UsuarioRegistroDTO(
        @NotEmpty(message = "El nombre no puede estar vacío.")
        String nombre,

        @NotEmpty(message = "El email no puede estar vacío.")
        @Email(message = "Debe ser un email válido.")
        String email,

        @NotEmpty(message = "La dirección no puede estar vacía.")
        String direccion,

        @NotNull(message = "La fecha de nacimiento es obligatoria.")
        LocalDate fecha_nacimiento,

        @NotEmpty(message = "Debe seleccionar un tipo de documento.")
        String tipo_documento,

        @NotEmpty(message = "El número de documento no puede estar vacío.")
        String numero_documento,

        @NotEmpty(message = "El teléfono no puede estar vacío.")
        @Size(min = 7, message = "El teléfono debe tener al menos 7 dígitos.")
        String telefono,

        @NotEmpty(message = "La contraseña no puede estar vacía.")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
        String password,
        Set<Integer> rolesId
) {}