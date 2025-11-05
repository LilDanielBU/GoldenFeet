package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer id; // <-- ¡Correcto! Es Integer.

    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(name = "original_price")
    private BigDecimal originalPrice;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "marca")
    private String marca;

    @Column(name = "rating", nullable = false)
    private Float rating;

    @Column(name = "destacado", nullable = false)
    private Boolean destacado;

    // --- CAMPO AÑADIDO (CORRECTO) ---
    @Column(name = "stock", nullable = false)
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // Se eliminó la relación con la entidad Inventario (¡Correcto!)

    /**
     * Constructor por defecto para inicializar valores.
     */
    public Producto() {
        this.rating = 0.0f;
        this.destacado = false;
        this.stock = 0; // Se inicializa el stock en 0
    }

    // Se eliminó el método @Transient getStock() (¡Correcto!)
}