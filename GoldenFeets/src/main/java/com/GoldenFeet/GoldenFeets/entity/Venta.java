package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

// --- ¡CORRECCIÓN CLAVE! ---
// Eliminamos @Data y usamos anotaciones específicas
@Getter
@Setter
@ToString(exclude = "detallesVenta") // Excluir para evitar bucle en logs
@EqualsAndHashCode(exclude = "detallesVenta") // Excluir para evitar bucle en comparaciones
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

    @ManyToOne(fetch = FetchType.LAZY) // LAZY es mejor para el rendimiento
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<DetalleVenta> detallesVenta = new HashSet<>();

    private String direccionEnvio;
    private String ciudadEnvio;
    private String metodoPago; // Ej: "TARJETA_CREDITO", "PAYPAL"
    private String idTransaccion; // Para guardar el ID de la pasarela de pago
}