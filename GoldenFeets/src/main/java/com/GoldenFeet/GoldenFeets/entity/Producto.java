package com.GoldenFeet.GoldenFeets.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonBackReference; // Si usas para evitar recursión en JSON

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "precio")
    private BigDecimal precio;

    @Column(name = "original_price")
    private BigDecimal originalPrice;

    // REMOVER esta línea o usarla de referencia si aún la necesitas,
    // pero la gestión real del stock se moverá a Inventario
    // @Column(name = "stock", nullable = false)
    // private Integer stock;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "marca")
    private String marca;

    @Column(name = "rating", nullable = false)
    private Float rating;

    @Column(name = "destacado", nullable = false)
    private Boolean destacado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    @JsonBackReference // Evita la recursión infinita en JSON si Categoria también tiene una lista de Productos
    private Categoria categoria;

    // AÑADE ESTO: Relación OneToOne con Inventario
    @OneToOne(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventario inventario;

    // Constructores, Getters y Setters
    public Producto() {
        this.rating = 0.0f;
        this.destacado = false;
        // this.stock = 0; // Inicializar stock si se mantiene aquí temporalmente
    }

    public Producto(String nombre, String descripcion, BigDecimal precio, BigDecimal originalPrice, String imagenUrl, String marca, Float rating, Boolean destacado, Categoria categoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.originalPrice = originalPrice;
        // this.stock = stock; // Si se sigue manejando aquí
        this.imagenUrl = imagenUrl;
        this.marca = marca;
        this.rating = rating != null ? rating : 0.0f;
        this.destacado = destacado != null ? destacado : false;
        this.categoria = categoria;
    }

    // Getters y Setters existentes...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    // Comentar o remover si el stock se mueve a Inventario
    /*
    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
    */

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public Boolean getDestacado() {
        return destacado;
    }

    public void setDestacado(Boolean destacado) {
        this.destacado = destacado;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    // AÑADE ESTO: Getter y Setter para inventario
    public Inventario getInventario() {
        return inventario;
    }

    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", originalPrice=" + originalPrice +
                // ", stock=" + stock + // Comentar o remover
                ", imagenUrl='" + imagenUrl + '\'' +
                ", marca='" + marca + '\'' +
                ", rating=" + rating +
                ", destacado=" + destacado +
                // ", categoria=" + (categoria != null ? categoria.getNombre() : "N/A") + // Puede causar recursión si no se maneja
                '}';
    }
}