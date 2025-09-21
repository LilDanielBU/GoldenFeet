package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // <-- Asegúrate de tener este import
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByRoles_Nombre(String nombreRol);

}