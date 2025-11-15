package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.AuthResponseDTO;
import com.GoldenFeet.GoldenFeets.dto.LoginRequestDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioRegistroDTO;

public interface AuthService {

    /**
     * Registra un nuevo usuario en el sistema.
     * @param request DTO con los datos del usuario a registrar.
     * @return un DTO con el token de autenticación para el nuevo usuario.
     */
    AuthResponseDTO register(UsuarioRegistroDTO request);

    /**
     * Autentica a un usuario existente.
     * @param request DTO con el email y la contraseña.
     * @return un DTO con el token de autenticación si las credenciales son correctas.
     */
    AuthResponseDTO login(LoginRequestDTO request);
}