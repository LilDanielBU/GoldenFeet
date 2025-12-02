package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // CORRECCIÓN: Ordenamos por ID descendente para que el último pedido creado (mayor ID) salga primero.
    @Query("SELECT DISTINCT v FROM Venta v " +
            "LEFT JOIN FETCH v.detallesVenta dv " +
            "LEFT JOIN FETCH dv.producto p " +
            "WHERE v.cliente.idUsuario = :idCliente " +
            "ORDER BY v.idVenta DESC")
    List<Venta> findByCliente_IdUsuario(@Param("idCliente") Integer idCliente);

    List<Venta> findByFechaVentaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT SUM(v.total) FROM Venta v")
    Double sumarTotalVentas();

    long countByEstado(String estado);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    Double sumarVentasPorRango(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    @Query("SELECT SUM(d.cantidad) FROM DetalleVenta d WHERE d.venta.fechaVenta BETWEEN :inicio AND :fin")
    Integer contarUnidadesVendidasRango(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}