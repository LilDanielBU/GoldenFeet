package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "variantes_producto")
public class VarianteProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @ToString.Exclude
    private Producto producto;

    private Integer talla;
    private String color;
    private Integer stock;

    @Column(name = "imagen_nombre")
    private String imagenNombre;

    // Método útil para mostrar en el panel
    public String getSku() {
        return producto.getNombre() + " - " + color + " (Talla " + talla + ")";
    }

    public BigDecimal getPrecio() {
        return producto.getPrecio() != null ? BigDecimal.valueOf(producto.getPrecio()) : BigDecimal.ZERO;
    }


}