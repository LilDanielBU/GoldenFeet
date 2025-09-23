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

    // --- CORRECCIÓN CLAVE AQUÍ ---
    // Se ajusta el tipo a Integer y se añade el nombre correcto de la columna.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer id;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @OneToOne(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventario inventario;

    public Producto() {
        this.rating = 0.0f;
        this.destacado = false;
    }

    // Método de conveniencia para obtener el stock desde el inventario
    @Transient
    public int getStock() {
        if (this.inventario != null) {
            return this.inventario.getStockActual();
        }
        return 0;
    }
}