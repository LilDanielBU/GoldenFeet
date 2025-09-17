package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca un usuario por su dirección de correo electrónico.
     * Spring Data JPA crea la consulta automáticamente a partir del nombre del método.
     *
     * @param email el email del usuario a buscar.
     * @return un Optional que contiene al usuario si se encuentra.
     */
    Optional<Usuario> findByEmail(String email);
}