package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.AdminUsuarioDTO;
import com.GoldenFeet.GoldenFeets.dto.AdminUsuarioUpdateDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioRegistroDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioResponseDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioUpdateDTO;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import java.util.List;

public interface UsuarioService {

    UsuarioResponseDTO actualizarPerfil(Integer idUsuario, UsuarioUpdateDTO request);
    UsuarioResponseDTO obtenerPerfil(Integer idUsuario);
    List<AdminUsuarioDTO> listarTodosLosUsuarios();
    UsuarioResponseDTO guardarUsuario(UsuarioRegistroDTO request);
    void eliminarUsuario(Integer idUsuario);
    void actualizarUsuarioAdmin(AdminUsuarioUpdateDTO dto);

    List<Usuario> findByRol(String nombreRol);
}