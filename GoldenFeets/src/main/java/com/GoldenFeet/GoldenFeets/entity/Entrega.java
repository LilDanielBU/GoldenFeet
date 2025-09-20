package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "entrega") // <-- CORRECCIÓN: Cambiado a singular
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrega")
    private Long idEntrega;

    // CORRECCIÓN: Mapeado a la columna 'fecha_entrega' de tu tabla
    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    @Column(length = 50)
    private String estado;

    @Column(columnDefinition = "TEXT")
    private String motivoCancelacion;

    @Column(columnDefinition = "TEXT")
    private String motivoRechazo;

    @OneToOne
    @JoinColumn(name = "id_venta", nullable = false, unique = true)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_distribuidor")
    private Usuario distribuidor;
}