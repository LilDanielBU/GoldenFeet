package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.*;

import java.util.List;

public interface UsuarioService {

    /**
     * Actualiza el perfil de un usuario existente.
     * @param idUsuario el ID del usuario a actualizar.
     * @param request DTO con los datos a modificar.
     * @return DTO con la información del usuario actualizado.
     */
    UsuarioResponseDTO actualizarPerfil(Integer idUsuario, UsuarioUpdateDTO request);

    /**
     * Obtiene la información del perfil de un usuario.
     * @param idUsuario el ID del usuario a buscar.
     * @return DTO con la información del perfil.
     */
    UsuarioResponseDTO obtenerPerfil(Integer idUsuario);
    List<AdminUsuarioDTO> listarTodosLosUsuarios();

    UsuarioResponseDTO guardarUsuario(UsuarioRegistroDTO request); // Reutilizamos el DTO de registro

    void eliminarUsuario(Integer idUsuario);
    void actualizarUsuarioAdmin(AdminUsuarioUpdateDTO dto);
}