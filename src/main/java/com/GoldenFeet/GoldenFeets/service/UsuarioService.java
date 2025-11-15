package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.AdminUsuarioDTO;
import com.GoldenFeet.GoldenFeets.dto.AdminUsuarioUpdateDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioRegistroDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioResponseDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioUpdateDTO;
import com.GoldenFeet.GoldenFeets.entity.Usuario;

import java.util.List;
import java.util.Map;

public interface UsuarioService {

    /**
     * Obtiene todos los usuarios (retorna entidades Usuario)
     */
    // SÓLO la declaración, sin implementación y SIN duplicidad
    List<Usuario> obtenerTodosLosUsuarios();


    long contarUsuariosActivos();

    // Métodos originales
    UsuarioResponseDTO actualizarPerfil(Integer idUsuario, UsuarioUpdateDTO request);
    UsuarioResponseDTO obtenerPerfil(Integer idUsuario);
    List<AdminUsuarioDTO> listarTodosLosUsuarios();

    UsuarioResponseDTO guardarUsuario(UsuarioRegistroDTO request);

    void eliminarUsuario(Integer idUsuario);
    Usuario actualizarUsuarioAdmin(AdminUsuarioUpdateDTO dto);
    List<Usuario> findByRol(String nombreRol);

    // Métodos nuevos agregados
    /**
     * Busca un usuario por su email
     */
    Usuario buscarPorEmail(String email);

    /**
     * Cambia el rol de un usuario existente
     */
    Usuario cambiarRolUsuario(Integer id, String nuevoRol);

    /**
     * Obtiene un usuario por ID (retorna entidad Usuario)
     */
    Usuario obtenerUsuarioPorId(Integer id);

    /**
     * Guarda un usuario directamente (entidad Usuario)
     */
    Usuario guardarUsuario(Usuario usuario);

    /**
     * Crea un cliente automáticamente desde una venta
     */
    Usuario crearClienteDesdeVenta(String nombre, String email, String telefono,
                                   String direccion, String ciudad, String codigoPostal);

    /**
     * Obtiene usuarios agrupados por rol
     */
    Map<String, List<Usuario>> obtenerUsuariosAgrupadosPorRol();

    /**
     * Valida si se puede asignar un rol específico a un usuario
     */
    boolean puedeAsignarRol(Integer usuarioId, String nombreRol);

    /**
     * Obtiene estadísticas de usuarios por rol
     */
    Map<String, Long> obtenerEstadisticasUsuariosPorRol();


        long contarUsuarios();

        long contarUsuariosInactivos();



}