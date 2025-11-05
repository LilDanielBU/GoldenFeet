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
    private Long idCategoria; // <-- Mantenido como Long (es lo correcto para tu app)

    @Column(nullable = false, unique = true) // Añadido para asegurar que el nombre sea único
    private String nombre;

    private String descripcion;

    // --- INICIO DE CORRECCIÓN ---
    // Se renombra el campo para almacenar solo el nombre del archivo
    @Column(name = "imagen_nombre")
    private String imagenNombre;
    // --- FIN DE CORRECCIÓN ---

    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Producto> productos;
}