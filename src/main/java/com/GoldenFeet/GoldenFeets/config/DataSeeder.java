package com.GoldenFeet.GoldenFeets.config;

import com.GoldenFeet.GoldenFeets.entity.Categoria; // Importación necesaria
import com.GoldenFeet.GoldenFeets.entity.Rol;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository; // Importación y Repositorio necesario
import com.GoldenFeet.GoldenFeets.repository.RolRepository;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List; // Importación necesaria
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoriaRepository categoriaRepository; // ✔ INYECCIÓN AÑADIDA

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

        // ----------------------------------------
        // ✔ 2. CREAR CATEGORÍAS (LÓGICA AÑADIDA)
        // ----------------------------------------
        if (categoriaRepository.count() == 0) {

            // CRÍTICO: Añadimos 'null' como quinto argumento para la List<Producto>
            Categoria hombre = new Categoria(null, "Hombre", "Calzado masculino de todo tipo.", "default_hombre.jpg", null);
            Categoria mujer = new Categoria(null, "Mujer", "Calzado femenino de todo tipo.", "default_mujer.jpg", null);
            Categoria ninos = new Categoria(null, "Niños", "Calzado infantil.", "default_ninos.jpg", null);

            categoriaRepository.saveAll(List.of(hombre, mujer, ninos));
            log.info("Categorías por defecto creadas: Hombre, Mujer, Niños");
        }

        // 3. Crear usuario administrador si no existe
        if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {
            Usuario adminUser = new Usuario();
            adminUser.setNombre("Administrador");
            adminUser.setEmail(adminEmail);
            adminUser.setPasswordHash(passwordEncoder.encode(adminPassword));
            adminUser.setRoles(Set.of(adminRole, clientRole));
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