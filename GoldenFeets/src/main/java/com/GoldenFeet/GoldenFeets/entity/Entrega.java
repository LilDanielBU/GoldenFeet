package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "entrega")
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrega")
    private Long idEntrega; // <-- CORRECCIÓN: El campo se llama idEntrega

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    @Column(length = 50)
    private String estado;

    @Column(columnDefinition = "TEXT")
    private String motivoCancelacion;

    @Column(columnDefinition = "TEXT")
    private String motivoRechazo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false, unique = true)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_distribuidor")
    private Usuario distribuidor;
}