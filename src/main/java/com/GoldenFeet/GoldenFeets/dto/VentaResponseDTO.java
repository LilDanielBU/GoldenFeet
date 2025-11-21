package com.GoldenFeet.GoldenFeets.dto;

import com.GoldenFeet.GoldenFeets.entity.DetalleVenta;
import com.GoldenFeet.GoldenFeets.entity.Venta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO para la respuesta, mostrando los detalles de la venta creada.
 */
public final class VentaResponseDTO {
    private final Long idVenta;
    private final LocalDate fechaVenta;
    private final BigDecimal total;
    private final String estado;
    private final String clienteEmail;
    private final List<DetalleVentaResponseDTO> detalles;

    public VentaResponseDTO(Long idVenta, LocalDate fechaVenta, BigDecimal total, String estado, String clienteEmail, List<DetalleVentaResponseDTO> detalles) {
        this.idVenta = idVenta;
        this.fechaVenta = fechaVenta;
        this.total = total;
        this.estado = estado;
        this.clienteEmail = clienteEmail;
        this.detalles = detalles;
    }

    /**
     * Factory method para mapear desde la entidad Venta.
     */
    public static VentaResponseDTO fromEntity(Venta venta) {
        List<DetalleVentaResponseDTO> detallesDto = venta.getDetallesVenta().stream()
                .map(DetalleVentaResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return new VentaResponseDTO(
                venta.getIdVenta(),
                venta.getFechaVenta(),
                venta.getTotal(),
                venta.getEstado(),
                venta.getCliente().getEmail(),
                detallesDto
        );
    }

    // Getters
    public Long getIdVenta() { return idVenta; }
    public LocalDate getFechaVenta() { return fechaVenta; }
    public BigDecimal getTotal() { return total; }
    public String getEstado() { return estado; }
    public String getClienteEmail() { return clienteEmail; }
    public List<DetalleVentaResponseDTO> getDetalles() { return detalles; }
}

/**
 * DTO para cada detalle en la respuesta.
 */
final class DetalleVentaResponseDTO {
    private final Integer productoId;
    private final String nombreProducto;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;

    public DetalleVentaResponseDTO(Integer productoId, String nombreProducto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    /**
     * Factory method para mapear desde la entidad DetalleVenta.
     */
    public static DetalleVentaResponseDTO fromEntity(DetalleVenta detalle) {
        return new DetalleVentaResponseDTO(
                // 💥 CORRECCIÓN AQUÍ: Convertimos Long a Integer
                detalle.getProducto().getId().intValue(),

                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal()
        );
    }

    // Getters
    public Integer getProductoId() { return productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}