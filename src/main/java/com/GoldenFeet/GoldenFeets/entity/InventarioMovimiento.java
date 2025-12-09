package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa la tabla 'inventario_movimientos'.
 * Cada instancia de esta clase es un registro en el historial
 * de stock de una variante de producto.
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "inventario_movimientos")
public class InventarioMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación con la entidad VarianteProducto.
     * Permite NULL para conservar historial incluso si la variante es eliminada.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id", nullable = true) // <-- CORREGIDO
    private VarianteProducto variante; // <-- CORREGIDO

    /**
     * Define el tipo de movimiento.
     */
    @Column(nullable = false, length = 50)
    private String tipoMovimiento;

    /**
     * Cantidad del movimiento.
     */
    @Column(nullable = false)
    private int cantidad;

    /**
     * Motivo o descripción para auditoría.
     */
    @Column(length = 255)
    private String motivo;

    /**
     * Fecha y hora exactas en que se registró el movimiento.
     */
    @Column(nullable = false)
    private LocalDateTime fecha;


    // --- Constructor actualizado ---
    public InventarioMovimiento(VarianteProducto variante, String tipoMovimiento, int cantidad, String motivo) {
        this.variante = variante;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.motivo = motivo;
    }

    /**
     * Se ejecuta automáticamente antes de persistir en la BD.
     */
    @PrePersist
    protected void onCreate() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }
}
