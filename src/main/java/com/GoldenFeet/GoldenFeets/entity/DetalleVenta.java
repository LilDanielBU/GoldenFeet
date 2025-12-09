package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "detalles_venta")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    @JsonBackReference
    private Venta venta;

    // --- CAMBIO IMPORTANTE: Reemplazamos Producto por VarianteProducto ---
    // Ya no mapeamos "id_producto" porque esa columna se eliminó en la base de datos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id", nullable = false)
    private VarianteProducto variante;

    @Column(name = "talla")
    private String talla;

    @Column(name = "color")
    private String color;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    // --- Constructores ---
    public DetalleVenta() {
    }

    // Constructor actualizado para recibir VarianteProducto
    public DetalleVenta(Venta venta, VarianteProducto variante, Integer cantidad, BigDecimal precioUnitario, String talla, String color) {
        this.venta = venta;
        this.variante = variante;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.talla = talla;
        this.color = color;
        if (precioUnitario != null && cantidad != null) {
            this.subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
        }
    }

    // --- Getters y Setters ---

    public Long getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    // === NUEVOS GETTERS Y SETTERS PARA VARIANTE ===
    public VarianteProducto getVariante() {
        return variante;
    }

    public void setVariante(VarianteProducto variante) {
        this.variante = variante;
    }

    // MÉTODOS DE COMPATIBILIDAD (Opcional):
    // Si tienes código antiguo llamando a getProducto(), esto ayuda a que no se rompa tanto,
    // obteniendo el producto a través de la variante.
    public Producto getProducto() {
        return variante != null ? variante.getProducto() : null;
    }
    // ==============================================

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        recalcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        recalcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    private void recalcularSubtotal() {
        if (this.precioUnitario != null && this.cantidad != null) {
            this.subtotal = this.precioUnitario.multiply(new BigDecimal(this.cantidad));
        }
    }

    @Override
    public String toString() {
        return "DetalleVenta{" +
                "idDetalle=" + idDetalle +
                ", variante=" + (variante != null ? variante.getSku() : "N/A") +
                ", cantidad=" + cantidad +
                '}';
    }
}