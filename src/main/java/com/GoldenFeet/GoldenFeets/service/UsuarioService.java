package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.AdminUsuarioDTO;
import com.GoldenFeet.GoldenFeets.dto.AdminUsuarioUpdateDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioRegistroDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioResponseDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioUpdateDTO;
import com.GoldenFeet.GoldenFeets.entity.Usuario;

import java.util.List;
import java.util.Map;
import java.util.Optional; // Necesario para Optional

public interface UsuarioService {

    // Métodos originales
    UsuarioResponseDTO actualizarPerfil(Integer idUsuario, UsuarioUpdateDTO request);
    UsuarioResponseDTO obtenerPerfil(Integer idUsuario);
    List<AdminUsuarioDTO> listarTodosLosUsuarios();

    // Método para guardar usuario desde registro
    UsuarioResponseDTO guardarUsuario(UsuarioRegistroDTO request);

    void eliminarUsuario(Integer idUsuario);
    Usuario actualizarUsuarioAdmin(AdminUsuarioUpdateDTO dto);
    List<Usuario> findByRol(String nombreRol);

    // 💥 MÉTODO CLAVE AÑADIDO: Debe estar presente para que UsuarioServiceImpl compile.
    Optional<Usuario> buscarPorId(Integer id);

    // Métodos nuevos agregados
    Usuario buscarPorEmail(String email);
    Usuario cambiarRolUsuario(Integer id, String nuevoRol);
    Usuario obtenerUsuarioPorId(Integer id);
    Usuario guardarUsuario(Usuario usuario);
    Usuario crearClienteDesdeVenta(String nombre, String email, String telefono,
                                   String direccion, String ciudad, String codigoPostal);
    Map<String, List<Usuario>> obtenerUsuariosAgrupadosPorRol();
    boolean puedeAsignarRol(Integer usuarioId, String nombreRol);
    Map<String, Long> obtenerEstadisticasUsuariosPorRol();
    List<Usuario> obtenerTodosLosUsuarios();
}