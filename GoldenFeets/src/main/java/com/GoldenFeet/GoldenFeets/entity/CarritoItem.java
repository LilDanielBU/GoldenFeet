package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "carrito_items")
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "session_id", nullable = false)
    private String sessionId; // O podría ser el id del usuario

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "fecha_agregado")
    private LocalDateTime fechaAgregado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @PrePersist
    protected void onAdd() {
        this.fechaAgregado = LocalDateTime.now();
    }

    // Getters y Setters
}