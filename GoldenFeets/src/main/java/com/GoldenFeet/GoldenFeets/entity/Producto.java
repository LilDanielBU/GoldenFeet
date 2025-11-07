package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // Asumo que lo tienes o lo necesitas
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "productos")
@NoArgsConstructor // ✅ Lombok genera el constructor sin argumentos
@AllArgsConstructor // Opcional, si lo necesitas para otras operaciones
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer id; // ID es Integer

    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(name = "original_price")
    private BigDecimal originalPrice;

    @Column(name = "imagen_nombre")
    private String imagenNombre;

    @Column(name = "marca")
    private String marca;

    @Column(name = "rating", nullable = false)
    private Float rating = 0.0f; // Inicialización en la declaración

    @Column(name = "destacado", nullable = false)
    private Boolean destacado = false; // Inicialización en la declaración

    @Column(name = "stock", nullable = false)
    private Integer stock = 0; // Inicialización en la declaración

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;


    // 🛑 SE ELIMINA EL CONSTRUCTOR MANUAL:
    /*
    public Producto() {
        this.rating = 0.0f;
        this.destacado = false;
        this.stock = 0;
    }
    */
}