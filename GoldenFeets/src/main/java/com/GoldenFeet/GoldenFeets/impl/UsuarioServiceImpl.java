package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.AdminUsuarioDTO;
import com.GoldenFeet.GoldenFeets.dto.AdminUsuarioUpdateDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioRegistroDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioResponseDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioUpdateDTO;
import com.GoldenFeet.GoldenFeets.entity.Rol;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.repository.RolRepository;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import com.GoldenFeet.GoldenFeets.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

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
    public UsuarioResponseDTO guardarUsuario(UsuarioRegistroDTO request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("El email ya está en uso.");
        }

        Set<Rol> roles = request.rolesId().stream()
                .map(id -> rolRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Rol no encontrado")))
                .collect(Collectors.toSet());

        if (roles.isEmpty()) {
            throw new IllegalArgumentException("Se debe seleccionar al menos un rol.");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(request.nombre());
        nuevoUsuario.setEmail(request.email());
        nuevoUsuario.setPasswordHash(passwordEncoder.encode(request.password()));
        nuevoUsuario.setDireccion(request.direccion());
        nuevoUsuario.setTelefono(request.telefono());
        nuevoUsuario.setRoles(roles);

        Usuario guardado = usuarioRepository.save(nuevoUsuario);
        return convertirAUsuarioResponseDTO(guardado);
    }

    // --- MÉTODO FALTANTE AÑADIDO ---
    @Override
    public void eliminarUsuario(Integer idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new EntityNotFoundException("No se encontró el usuario para eliminar");
        }
        usuarioRepository.deleteById(idUsuario);
    }

    // --- MÉTODO FALTANTE AÑADIDO ---
    @Override
    @Transactional
    public void actualizarUsuarioAdmin(AdminUsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.idUsuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + dto.idUsuario()));

        usuario.setNombre(dto.nombre());
        usuario.setActivo(dto.activo());

        if (dto.rolesId() != null && !dto.rolesId().isEmpty()) {
            Set<Rol> nuevosRoles = dto.rolesId().stream()
                    .map(rolId -> rolRepository.findById(rolId)
                            .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con ID: " + rolId)))
                    .collect(Collectors.toSet());
            usuario.setRoles(nuevosRoles);
        }

        usuarioRepository.save(usuario);
    }


    // --- MAPPERS ---
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
                usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toSet())
        );
    }
}