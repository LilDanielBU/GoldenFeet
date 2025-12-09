package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id;

    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Double precio;
    private Double originalPrice;

    private String marca;
    private Boolean destacado;
    private Integer rating;

    @Column(name = "imagen_nombre")
    private String imagenNombre; // Imagen Principal

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // --- CAMPO CRÍTICO ---
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VarianteProducto> variantes = new ArrayList<>();

    // ********************************************
    // CORRECCIÓN CRÍTICA: Setter Bidireccional
    // ********************************************
    // Sobreescribimos el setter de Lombok para garantizar que cada variante
    // sepa quién es su padre (Producto) antes de que la colección sea guardada.
    public void setVariantes(List<VarianteProducto> variantes) {
        if (this.variantes == null) {
            this.variantes = new ArrayList<>();
        }
        this.variantes.clear();
        if (variantes != null) {
            for (VarianteProducto variante : variantes) {
                this.variantes.add(variante);
                variante.setProducto(this); // <--- ESTO ES VITAL
            }
        }
    }
    // ********************************************

    public Integer getStockTotal() {
        if (this.variantes == null || this.variantes.isEmpty()) {
            return 0;
        }
        return this.variantes.stream()
                .mapToInt(VarianteProducto::getStock)
                .sum();
    }
}