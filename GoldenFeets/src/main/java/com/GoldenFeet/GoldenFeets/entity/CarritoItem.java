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
    private String sessionId; // O el ID del usuario si ha iniciado sesión

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "fecha_agregado")
    private LocalDateTime fechaAgregado;

    // --- RELACIÓN CORREGIDA Y MEJORADA ---
    @ManyToOne(fetch = FetchType.LAZY) // Usamos LAZY para mejor rendimiento
    @JoinColumn(name = "producto_id", referencedColumnName = "id", nullable = false)
    private Producto producto;

    @PrePersist
    protected void onAdd() {
        this.fechaAgregado = LocalDateTime.now();
    }

}