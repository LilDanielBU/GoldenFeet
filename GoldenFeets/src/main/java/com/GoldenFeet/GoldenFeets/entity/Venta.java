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

    @Column(name = "fecha_venta", nullable = false)
    private LocalDate fechaVenta;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "direccion_envio")
    private String direccionEnvio;

    @Column(name = "ciudad_envio")
    private String ciudadEnvio;

    /** Representa la localidad o comuna para la gestión de entrega. */
    @Column(name = "localidad")
    private String localidad;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "id_transaccion")
    private String idTransaccion;

    // Relación Bidireccional con DetalleVenta
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<DetalleVenta> detallesVenta = new ArrayList<>();

    // --- MÉTODOS DE AYUDA para la gestión de la colección ---
    public void addDetalle(DetalleVenta detalle) {
        if (detallesVenta == null) {
            detallesVenta = new ArrayList<>();
        }
        detallesVenta.add(detalle);
        detalle.setVenta(this);
    }

    public void removeDetalle(DetalleVenta detalle) {
        if (detallesVenta != null) {
            detallesVenta.remove(detalle);
            detalle.setVenta(null);
        }
    }
}