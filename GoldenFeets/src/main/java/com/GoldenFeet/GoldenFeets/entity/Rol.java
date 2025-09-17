package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Data; // <-- ¡IMPORTANTE AÑADIR ESTA ANOTACIÓN!

@Data // <-- Genera getters, setters, toString, etc. automáticamente.
@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;
}