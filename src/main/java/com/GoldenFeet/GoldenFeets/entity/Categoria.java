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
    @Column(name = "id_categoria")
    private Integer idCategoria;

    private String nombre;

    private String descripcion;

    // 💥 CAMBIO REALIZADO: De 'imagenUrl' a 'imagenNombre'
    // Esto genera automáticamente los métodos getImagenNombre() y setImagenNombre() gracias a @Data
    @Column(name = "imagen_nombre")
    private String imagenNombre;

    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Producto> productos;
}