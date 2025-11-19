package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "novedades_entrega")
public class Novedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    // Relación: Muchas novedades pueden pertenecer a una entrega
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entrega", nullable = false)
    private Entrega entrega;

    // Quién reportó la novedad (el distribuidor o gerente)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_reporta")
    private Usuario usuarioReporta;
}