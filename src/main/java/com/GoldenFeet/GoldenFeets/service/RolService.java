package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.entity.Rol;
import java.util.List;
import java.util.Optional;

public interface RolService {
    List<Rol> listarTodosLosRoles();
    Optional<Rol> obtenerRolPorNombre(String nombre);
}