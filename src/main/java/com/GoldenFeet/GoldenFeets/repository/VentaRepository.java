package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // 💥 MÉTODO CORREGIDO
    // Antes: LEFT JOIN FETCH dv.producto p
    // Ahora: LEFT JOIN FETCH dv.variante var LEFT JOIN FETCH var.producto p
    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.detallesVenta dv LEFT JOIN FETCH dv.variante var LEFT JOIN FETCH var.producto p WHERE v.cliente.idUsuario = :idCliente ORDER BY v.idVenta DESC")
    List<Venta> findByCliente_IdUsuario(Integer idCliente);

    // Los métodos siguientes (si usan HQL) TAMBIÉN DEBEN ser revisados si fallan:

    @Query("SELECT SUM(v.total) FROM Venta v")
    Double sumarTotalVentas();

    long countByEstado(String estado);

    List<Venta> findByFechaVentaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    Double sumarVentasPorRango(LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT SUM(dv.cantidad) FROM Venta v JOIN v.detallesVenta dv WHERE v.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    Integer contarUnidadesVendidasRango(LocalDate fechaInicio, LocalDate fechaFin);
}