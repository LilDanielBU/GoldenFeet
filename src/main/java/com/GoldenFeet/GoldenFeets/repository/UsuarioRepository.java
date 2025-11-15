package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByRoles_Nombre(String nombreRol);

    long countByActivoTrue();

    @Query("SELECT u FROM Usuario u JOIN FETCH u.roles")
    List<Usuario> findAllWithRoles();

    long countByActivo(boolean b);
}