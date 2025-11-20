package com.GoldenFeet.GoldenFeets.config;

import com.GoldenFeet.GoldenFeets.entity.Rol;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.repository.RolRepository;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j // Para poder usar logs (opcional pero recomendado)
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email}")
    private String adminEmail;

    @Value("${admin.default.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        // 1. Crear roles si no existen
        Rol adminRole = createRoleIfNotFound("ROLE_ADMIN");
        Rol clientRole = createRoleIfNotFound("ROLE_CLIENTE");
        Rol empleadoRole = createRoleIfNotFound("ROLE_EMPLEADO");
        Rol distribuidorRole = createRoleIfNotFound("ROLE_DISTRIBUIDOR");
        Rol gerenteEntregas = createRoleIfNotFound("ROLE_GERENTEENTREGAS");
        Rol gerenteInventario = createRoleIfNotFound("ROLE_GERENTEINVENTARIO");


        // 2. Crear usuario administrador si no existe
        if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {
            Usuario adminUser = new Usuario();
            adminUser.setNombre("Administrador");
            adminUser.setEmail(adminEmail);
            adminUser.setPasswordHash(passwordEncoder.encode(adminPassword));
            adminUser.setRoles(Set.of(adminRole, clientRole)); // El admin también puede ser cliente
            adminUser.setActivo(true);

            usuarioRepository.save(adminUser);
            log.info("Usuario administrador por defecto creado con email: {}", adminEmail);
        } else {
            log.info("El usuario administrador ya existe.");
        }
    }

    private Rol createRoleIfNotFound(String name) {
        return rolRepository.findByNombre(name)
                .orElseGet(() -> {
                    Rol newRole = new Rol();
                    newRole.setNombre(name);
                    return rolRepository.save(newRole);
                });
    }
}