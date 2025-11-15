package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.*;
import com.GoldenFeet.GoldenFeets.entity.Rol;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.repository.RolRepository;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import com.GoldenFeet.GoldenFeets.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public long contarUsuariosActivos() {
        return usuarioRepository.countByActivoTrue();
    }

    @Override
    public UsuarioResponseDTO actualizarPerfil(Integer idUsuario, UsuarioUpdateDTO request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        usuario.setNombre(request.nombre());
        usuario.setTelefono(request.telefono());
        usuario.setDireccion(request.direccion());
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return convertirAUsuarioResponseDTO(usuarioActualizado);
    }

    @Override
    public UsuarioResponseDTO obtenerPerfil(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        return convertirAUsuarioResponseDTO(usuario);
    }

    @Override
    public List<AdminUsuarioDTO> listarTodosLosUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirAAdminUsuarioDTO)
                .collect(Collectors.toList());
    }
    @Override
    public long contarUsuarios() {
        return usuarioRepository.count();
    }


    @Override
    public long contarUsuariosInactivos() {
        return usuarioRepository.countByActivo(false);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO guardarUsuario(UsuarioRegistroDTO request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("El email ya está en uso.");
        }
        Set<Rol> roles = request.rolesId().stream()
                .map(id -> rolRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con ID: " + id)))
                .collect(Collectors.toSet());

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(request.nombre());
        nuevoUsuario.setEmail(request.email());
        nuevoUsuario.setPasswordHash(passwordEncoder.encode(request.password()));
        nuevoUsuario.setDireccion(request.direccion());
        nuevoUsuario.setTelefono(request.telefono());
        nuevoUsuario.setFechaNacimiento(request.fecha_nacimiento());
        nuevoUsuario.setLocalidad(request.localidad()); // <-- LÍNEA AÑADIDA
        // Asumiendo que 'tipo_documento' y 'numero_documento' también están en la entidad Usuario
        // nuevoUsuario.setTipoDocumento(request.tipo_documento());
        // nuevoUsuario.setNumeroDocumento(request.numero_documento());
        nuevoUsuario.setRoles(roles);
        nuevoUsuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(nuevoUsuario);
        return convertirAUsuarioResponseDTO(guardado);
    }

    @Override
    public void eliminarUsuario(Integer idUsuario) {
        usuarioRepository.deleteById(idUsuario);
    }

    @Override
    @Transactional
    public Usuario actualizarUsuarioAdmin(AdminUsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + dto.getIdUsuario()));

        usuario.setNombre(dto.getNombre());
        usuario.setActivo(dto.isActivo());
        usuario.setLocalidad(dto.getLocalidad()); // <-- LÍNEA AÑADIDA

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getRolesId() != null && !dto.getRolesId().isEmpty()) {
            Set<Rol> nuevosRoles = dto.getRolesId().stream()
                    .map(rolId -> rolRepository.findById(rolId)
                            .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con ID: " + rolId)))
                    .collect(Collectors.toSet());
            usuario.setRoles(nuevosRoles);
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public List<Usuario> findByRol(String nombreRol) {
        return usuarioRepository.findByRoles_Nombre(nombreRol);
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    @Override
    @Transactional
    public Usuario cambiarRolUsuario(Integer id, String nuevoRol) {
        Usuario usuario = obtenerUsuarioPorId(id);
        String rolNormalizado = nuevoRol.startsWith("ROLE_") ? nuevoRol : "ROLE_" + nuevoRol;
        Rol rol = rolRepository.findByNombre(rolNormalizado)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + rolNormalizado));
        usuario.setRoles(Set.of(rol));
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario obtenerUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public List<Usuario> obtenerTodosLosUsuarios() {

        return usuarioRepository.findAllWithRoles();
    }

    @Override
    @Transactional
    public Usuario crearClienteDesdeVenta(String nombre, String email, String telefono, String direccion, String ciudad, String codigoPostal) {
        // ... (tu método existente) ...
        return null; // O la lógica que tuvieras
    }

    @Override
    public Map<String, List<Usuario>> obtenerUsuariosAgrupadosPorRol() {
        return usuarioRepository.findAll().stream()
                .flatMap(u -> u.getRoles().stream().map(r -> new AbstractMap.SimpleEntry<>(r.getNombre(), u)))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    @Override
    public boolean puedeAsignarRol(Integer usuarioId, String nombreRol) {
        return rolRepository.findByNombre(nombreRol).isPresent();
    }

    @Override
    public Map<String, Long> obtenerEstadisticasUsuariosPorRol() {
        return rolRepository.findAll().stream()
                .collect(Collectors.toMap(Rol::getNombre, rol -> (long) rol.getUsuarios().size()));
    }

    private AdminUsuarioDTO convertirAAdminUsuarioDTO(Usuario usuario) {
        return new AdminUsuarioDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.isActivo(),
                usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toSet())
        );
    }

    private UsuarioResponseDTO convertirAUsuarioResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                usuario.isActivo(),
                usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toSet())
        );
    }
}