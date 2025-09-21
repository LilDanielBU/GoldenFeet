package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(length = 1000) // Buena práctica para descripciones largas
    private String descripcion;

    // Usar BigDecimal para dinero es la mejor práctica para evitar errores de precisión
    private BigDecimal precio;

    @Column(name = "original_price")
    private BigDecimal originalPrice;

    private String marca;
    private int stock;
    private double rating;
    private boolean destacado;

    @Column(name = "imagen_url")
    private String imagenUrl;

    // Relación: Muchos productos pertenecen a una categoría
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
}