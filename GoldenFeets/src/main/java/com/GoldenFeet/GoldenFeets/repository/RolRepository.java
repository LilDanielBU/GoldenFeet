package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    /**
     * Busca un rol por su nombre.
     * @param nombre el nombre del rol (ej: "ROLE_CLIENTE").
     * @return un Optional que contiene el rol si se encuentra.
     */
    Optional<Rol> findByNombre(String nombre);
}