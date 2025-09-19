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
    private Integer idCategoria;

    private String nombre;
    private String descripcion;

    // --- ¡AQUÍ ESTÁ EL CAMPO QUE FALTABA! ---
    @Column(name = "imagen_url") // Es buena práctica nombrar la columna en la base de datos
    private String imagenUrl;

    // Esta relación es correcta para el futuro, pero no soluciona el error actual.
    @OneToMany(mappedBy = "categoria")
    private List<Producto> productos;
}