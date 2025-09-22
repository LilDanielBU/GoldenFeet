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
    private Long id;

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

    // --- CAMPOS RESTAURADOS ---
    @Column(name = "marca")
    private String marca;

    @Column(name = "rating", nullable = false)
    private Float rating;

    @Column(name = "destacado", nullable = false)
    private Boolean destacado;
    // --- FIN DE CAMPOS RESTAURADOS ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @OneToOne(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventario inventario;

    // --- Constructor ---
    public Producto() {
        this.rating = 0.0f;
        this.destacado = false;
    }

    // --- Método de conveniencia para obtener el Stock ---
    @Transient
    public int getStock() {
        if (this.inventario != null) {
            return this.inventario.getCantidad();
        }
        return 0;
    }
}