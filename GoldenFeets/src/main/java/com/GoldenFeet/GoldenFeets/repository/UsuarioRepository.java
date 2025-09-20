package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // <-- Asegúrate de tener este import
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    // --- AÑADE ESTE NUEVO MÉTODO ---
    // Spring Data JPA creará una consulta que busca usuarios cuyo conjunto de roles
    // contenga un rol con el nombre especificado.
    List<Usuario> findByRoles_Nombre(String nombreRol);
}