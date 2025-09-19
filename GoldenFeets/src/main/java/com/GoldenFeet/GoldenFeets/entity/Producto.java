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
        private Long id; // La clave primaria se llama 'id'

        private String nombre;

        @Column(length = 1000)
        private String descripcion;

        private BigDecimal precio;

        private int stock;

        private String imagenUrl;

        @ManyToOne
        @JoinColumn(name = "categoria_id")
        private Categoria categoria;

        private boolean destacado;

        private BigDecimal originalPrice;
        private double rating;
    }