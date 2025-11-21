package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // 💥 CORRECCIÓN CRÍTICA:
    // Agregamos esto para que Hibernate encuentre la columna correcta
    // y coincida con las referencias de DetalleVenta e Inventario.
    @Column(name = "id_producto")
    private Long id;

    private String nombre;

    // A veces la descripción es larga, es bueno asegurar el tipo TEXT en la BD
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Double precio;
    private Double originalPrice;
    private Integer stock;
    private String marca;
    private Boolean destacado;
    private Integer rating;

    @Column(name = "imagen_nombre")
    private String imagenNombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
}