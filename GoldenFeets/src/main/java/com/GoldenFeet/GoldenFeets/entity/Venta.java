package com.GoldenFeet.GoldenFeets.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @Column(name = "fecha_venta")
    private LocalDate fechaVenta;

    @Column(name = "total")
    private BigDecimal total;

    @Column(name = "estado")
    private String estado;

    @Column(name = "direccion_envio")
    private String direccionEnvio;

    @Column(name = "ciudad_envio")
    private String ciudadEnvio;

    // --- CAMPO DE LOCALIDAD AÑADIDO ---
    @Column(name = "localidad")
    private String localidad;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "id_transaccion")
    private String idTransaccion;

    // --- CORRECCIÓN: Cambiado de Set a List ---
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<DetalleVenta> detallesVenta = new ArrayList<>();

    // --- MÉTODOS DE AYUDA ---
    public void addDetalle(DetalleVenta detalle) {
        detallesVenta.add(detalle);
        detalle.setVenta(this);
    }

    public void removeDetalle(DetalleVenta detalle) {
        detallesVenta.remove(detalle);
        detalle.setVenta(null);
    }
}