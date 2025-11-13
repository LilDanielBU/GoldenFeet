package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@Entity
@Table(name = "ordencompra")
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Integer idOrden;

    @Column(name = "fecha_orden")
    private LocalDate fechaOrden;

    @Column(name = "estado", length = 50)
    private String estado;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    // --- Relación actualizada ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", referencedColumnName = "id_usuario")
    private Usuario proveedor;

    // --- Otras relaciones no cambian ---
    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL)
    private Set<DetalleOrdenCompra> detallesOrdenCompra;

    // Getters y Setters
}