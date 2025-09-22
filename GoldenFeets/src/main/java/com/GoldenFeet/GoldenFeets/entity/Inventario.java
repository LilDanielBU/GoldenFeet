package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    private Long idInventario;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", referencedColumnName = "id", unique = true, nullable = false)
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

    // Getters y Setters
    public Long getIdInventario() {
        return idInventario;
    }

    public void setIdInventario(Long idInventario) {
        this.idInventario = idInventario;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    // Métodos para facilitar la gestión del stock
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

    @Override
    public String toString() {
        return "Inventario{" +
                "idInventario=" + idInventario +
                ", producto=" + (producto != null ? producto.getNombre() : "N/A") +
                ", stockActual=" + stockActual +
                ", ultimaActualizacion=" + ultimaActualizacion +
                '}';
    }
}