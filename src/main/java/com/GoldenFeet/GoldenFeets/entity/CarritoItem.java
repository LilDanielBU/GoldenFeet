package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "carrito_items")
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "fecha_agregado")
    private LocalDateTime fechaAgregado;

    @ManyToOne(fetch = FetchType.LAZY)
    // --- CORRECCIÓN AQUÍ ---
    @JoinColumn(name = "producto_id", referencedColumnName = "id_producto", nullable = false)
    private Producto producto;

    @PrePersist
    protected void onAdd() {
        this.fechaAgregado = LocalDateTime.now();
    }

    // Cambia la relación @ManyToOne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id", nullable = false)
    private VarianteProducto variante; // En lugar de Producto

}