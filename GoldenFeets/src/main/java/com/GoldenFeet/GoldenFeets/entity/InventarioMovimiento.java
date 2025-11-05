package com.GoldenFeet.GoldenFeets.entity;

// Importaciones para Spring Boot 3+ (Jakarta)
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.PrePersist; // <--- 1. IMPORTA @PrePersist

// --- CAMBIO CLAVE: Usamos la moderna API de Java Time ---
import java.time.LocalDateTime;

// --- Adición: Usamos Lombok para simplificar el código ---
import lombok.Data;
import lombok.NoArgsConstructor; // Añadido para el constructor vacío

/**
 * Entidad que representa la tabla 'inventario_movimientos'.
 * Cada instancia de esta clase es un registro en el historial
 * de stock de un producto.
 */
@Entity
@Data // Genera automáticamente Getters, Setters, toString, hashCode, equals
@NoArgsConstructor // Genera el constructor vacío requerido por JPA
@Table(name = "inventario_movimientos")
public class InventarioMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación con la entidad Producto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

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
     * --- CAMBIO CRÍTICO: De Date a LocalDateTime ---
     */
    @Column(nullable = false)
    private LocalDateTime fecha;


    // --- Constructor para creación rápida ---

    /**
     * Constructor para crear un movimiento fácilmente desde el servicio.
     */
    public InventarioMovimiento(Producto producto, String tipoMovimiento, int cantidad, String motivo) {
        this.producto = producto;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.motivo = motivo;
        // La fecha AHORA se establecerá automáticamente gracias a @PrePersist
    }

    // --- INICIO DE CORRECCIÓN ---
    /**
     * Este método se ejecutará automáticamente JUSTO ANTES
     * de que un nuevo movimiento sea guardado en la BD.
     * Asignará la fecha y hora actual al campo 'fecha'.
     */
    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now(); // <--- 2. ASIGNA LA FECHA ACTUAL
    }
    // --- FIN DE CORRECCIÓN ---

    // NOTA: Se eliminan todos los Getters y Setters porque @Data de Lombok los genera.
}