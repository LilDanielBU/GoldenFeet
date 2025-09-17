package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.*;

import java.util.List;

public interface UsuarioService {

    UsuarioResponseDTO actualizarPerfil(Integer idUsuario, UsuarioUpdateDTO request);

    UsuarioResponseDTO obtenerPerfil(Integer idUsuario);
    List<AdminUsuarioDTO> listarTodosLosUsuarios();

    UsuarioResponseDTO guardarUsuario(UsuarioRegistroDTO request); // Reutilizamos el DTO de registro

    void eliminarUsuario(Integer idUsuario);
    void actualizarUsuarioAdmin(AdminUsuarioUpdateDTO dto);
}