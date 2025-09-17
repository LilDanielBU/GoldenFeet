package com.GoldenFeet.GoldenFeets.dto;

import java.util.Set;

public record AdminUsuarioUpdateDTO(
        Integer idUsuario,
        String nombre,
        boolean activo,
        Set<Integer> rolesId
) {}