package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString(exclude = "producto") // Excluir producto para evitar bucles en logs
@Entity
@Table(name = "inventario")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    private Long idInventario;

    @OneToOne(fetch = FetchType.LAZY)
    // --- CORRECCIÓN: Apuntar a la columna correcta de la tabla productos ---
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto", unique = true, nullable = false)
    private Producto producto;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    // Constructores
    public Inventario() {
    }

    public Inventario(Producto producto, Integer stockActual) {
        this.producto = producto;
        this.stockActual = stockActual;
        this.ultimaActualizacion = LocalDateTime.now();
    }

    // El método getCantidad() fue eliminado porque no pertenecía a esta entidad.
    // Los Getters y Setters ahora son generados por Lombok (@Getter y @Setter).

    // --- LÓGICA DE STOCK (YA ERA CORRECTA) ---
    public void reducirStock(int cantidad) {
        if (this.stockActual < cantidad) {
            throw new IllegalArgumentException("No hay suficiente stock para el producto " + producto.getNombre());
        }
        this.stockActual -= cantidad;
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public void aumentarStock(int cantidad) {
        this.stockActual += cantidad;
        this.ultimaActualizacion = LocalDateTime.now();
    }
}