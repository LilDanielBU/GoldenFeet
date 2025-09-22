package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List; // <-- Cambiado de Set a List

@Getter
@Setter
@ToString(exclude = "detallesVenta")
@EqualsAndHashCode(exclude = "detallesVenta")
@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long idVenta;

    private LocalDate fechaVenta;
    private BigDecimal total;
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    // --- CORRECCIÓN CLAVE: Se cambia Set por List ---
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetalleVenta> detallesVenta = new ArrayList<>();

    private String direccionEnvio;
    private String ciudadEnvio;
    private String metodoPago;
    private String idTransaccion;

    // --- GETTER Y SETTER CORREGIDOS PARA USAR LIST ---
    public List<DetalleVenta> getDetallesVenta() {
        return detallesVenta;
    }

    public void setDetallesVenta(List<DetalleVenta> detallesVenta) {
        this.detallesVenta = detallesVenta;
    }
}