package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // --- MÉTODO AÑADIDO ---
    List<Venta> findByCliente_IdUsuario(Integer idCliente);

    // --- MÉTODO AÑADIDO ---
    List<Venta> findByFechaVentaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT SUM(v.total) FROM Venta v")
    Double sumarTotalVentas();

    long countByEstado(String estado);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    Double sumarVentasPorRango(LocalDate inicio, LocalDate fin);

    @Query("SELECT SUM(d.cantidad) FROM DetalleVenta d WHERE d.venta.fechaVenta BETWEEN :inicio AND :fin")
    Integer contarUnidadesVendidasRango(LocalDate inicio, LocalDate fin);


}