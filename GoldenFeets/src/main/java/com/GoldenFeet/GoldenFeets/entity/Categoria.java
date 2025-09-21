package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategoria; // CORRECCIÓN: Cambiado de Integer a Long

    private String nombre;

    private String descripcion;

    @Column(name = "imagen_url")
    private String imagenUrl;

    // Relación bidireccional: Una categoría tiene muchos productos
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Producto> productos;
}