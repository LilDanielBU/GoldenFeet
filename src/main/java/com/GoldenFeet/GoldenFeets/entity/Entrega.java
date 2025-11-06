package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@Entity
@Table(name = "entrega")
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrega")
    private Long idEntrega;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(length = 50)
    private String estado;

    // --- CAMPO NUEVO AÑADIDO ---
    @Column(name = "localidad")
    private String localidad;
    // --- FIN CAMPO NUEVO ---

    @Column(columnDefinition = "TEXT")
    private String motivoCancelacion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false, unique = true)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_distribuidor")
    private Usuario distribuidor;

    @OneToMany(mappedBy = "entrega", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Novedad> novedades = new ArrayList<>();
}