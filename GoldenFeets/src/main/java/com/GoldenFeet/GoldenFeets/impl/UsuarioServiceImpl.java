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
import com.GoldenFeet.GoldenFeets.service.RolService;
import com.GoldenFeet.GoldenFeets.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolService rolService;

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

        // Manejar roles correctamente
        Set<Rol> roles;
        if (request.rolesId() != null && !request.rolesId().isEmpty()) {
            // Si se proporcionan IDs de roles
            roles = request.rolesId().stream()
                    .map(id -> rolRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con ID: " + id)))
                    .collect(Collectors.toSet());
        } else {
            // Rol por defecto: CLIENTE
            Rol rolCliente = rolRepository.findByNombre("ROLE_CLIENTE")
                    .orElseThrow(() -> new EntityNotFoundException("Rol ROLE_CLIENTE no encontrado en el sistema"));
            roles = Set.of(rolCliente);
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(request.nombre());
        nuevoUsuario.setEmail(request.email());
        nuevoUsuario.setPasswordHash(passwordEncoder.encode(request.password()));
        nuevoUsuario.setDireccion(request.direccion());
        nuevoUsuario.setTelefono(request.telefono());
        nuevoUsuario.setFechaNacimiento(request.fecha_nacimiento());
        nuevoUsuario.setRoles(roles);
        nuevoUsuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(nuevoUsuario);
        return convertirAUsuarioResponseDTO(guardado);
    }

    @Override
    public void eliminarUsuario(Integer idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new EntityNotFoundException("No se encontró el usuario para eliminar");
        }
        usuarioRepository.deleteById(idUsuario);
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    @Override
    public Usuario cambiarRolUsuario(Integer id, String nuevoRol) {
        // Validar entrada
        if (id == null || nuevoRol == null || nuevoRol.trim().isEmpty()) {
            throw new IllegalArgumentException("ID y rol no pueden ser nulos o vacíos");
        }

        // Buscar usuario
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Normalizar nombre del rol (agregar ROLE_ si no lo tiene)
        String rolNormalizado = nuevoRol.startsWith("ROLE_") ? nuevoRol : "ROLE_" + nuevoRol;

        // Buscar el rol usando el repository directamente
        Rol rol = rolRepository.findByNombre(rolNormalizado)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolNormalizado));

        // Asignar nuevo rol (reemplaza roles existentes)
        usuario.setRoles(Set.of(rol));

        // Guardar cambios
        return usuarioRepository.save(usuario);
    }

    @Override
    public List<Usuario> findByRol(String nombreRol) {
        // Normalizar nombre del rol
        String rolNormalizado = nombreRol.startsWith("ROLE_") ? nombreRol : "ROLE_" + nombreRol;

        // Usar método de repository si existe, sino usar filtrado manual
        try {
            return usuarioRepository.findByRoles_Nombre(rolNormalizado);
        } catch (Exception e) {
            // Fallback: filtrado manual
            return usuarioRepository.findAll().stream()
                    .filter(usuario -> usuario.getRoles().stream()
                            .anyMatch(rol -> rol.getNombre().equals(rolNormalizado)))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Usuario obtenerUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario crearClienteDesdeVenta(String nombre, String email, String telefono,
                                          String direccion, String ciudad, String codigoPostal) {
        // Verificar si ya existe
        Usuario existente = buscarPorEmail(email);
        if (existente != null) {
            return existente;
        }

        // Crear DTO para nuevo cliente - ajustado a tu estructura de DTO
        UsuarioRegistroDTO clienteDTO = new UsuarioRegistroDTO(
                nombre,
                email,
                direccion != null ? direccion : "",
                null, // fecha_nacimiento - será null
                "CC", // tipo_documento por defecto
                "00000000", // numero_documento temporal
                telefono != null ? telefono : "",
                generarPasswordTemporal(),
                Set.of(1) // Assuming ROLE_CLIENTE has ID 1, you may need to adjust this
        );

        // Usar el método existente para guardar
        UsuarioResponseDTO resultado = guardarUsuario(clienteDTO);

        // Retornar la entidad Usuario
        return obtenerUsuarioPorId(resultado.idUsuario());
    }

    @Override
    public Map<String, List<Usuario>> obtenerUsuariosAgrupadosPorRol() {
        List<Usuario> todosLosUsuarios = usuarioRepository.findAll();
        Map<String, List<Usuario>> usuariosPorRol = new HashMap<>();

        for (Usuario usuario : todosLosUsuarios) {
            for (Rol rol : usuario.getRoles()) {
                String nombreRol = rol.getNombre();
                usuariosPorRol.computeIfAbsent(nombreRol, k -> new ArrayList<>()).add(usuario);
            }
        }

        return usuariosPorRol;
    }

    @Override
    public boolean puedeAsignarRol(Integer usuarioId, String nombreRol) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        if (usuario == null) {
            return false;
        }

        // Validar que el rol existe
        String rolNormalizado = nombreRol.startsWith("ROLE_") ? nombreRol : "ROLE_" + nombreRol;
        return rolRepository.findByNombre(rolNormalizado).isPresent();
    }

    @Override
    public Map<String, Long> obtenerEstadisticasUsuariosPorRol() {
        Map<String, List<Usuario>> usuariosPorRol = obtenerUsuariosAgrupadosPorRol();
        Map<String, Long> estadisticas = new HashMap<>();
        usuariosPorRol.forEach((rol, usuarios) -> {
            estadisticas.put(rol, (long) usuarios.size());
        });
        return estadisticas;
    }

    @Override
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional
    public Usuario actualizarUsuarioAdmin(AdminUsuarioUpdateDTO dto) {
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

        return usuarioRepository.save(usuario);
    }

    // --- MÉTODOS AUXILIARES ---

    /**
     * Genera una contraseña temporal para clientes creados automáticamente
     */
    private String generarPasswordTemporal() {
        return "Cliente" + System.currentTimeMillis() % 10000;
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
                usuario.isActivo(),
                usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toSet())
        );
    }
}