package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor; // ✔ Necesario para el constructor de 4 argumentos en DataSeeder
import lombok.Data;
import lombok.NoArgsConstructor; // ✔ Necesario para Hibernate
import java.util.List;
import java.io.Serializable;

@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor // Genera el constructor sin argumentos (requerido por JPA/Hibernate)
@AllArgsConstructor // Genera el constructor con todos los argumentos (requerido por DataSeeder)
public class Categoria implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    private String nombre;

    private String descripcion;

    @Column(name = "imagen_nombre")
    private String imagenNombre;

    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Producto> productos;
}