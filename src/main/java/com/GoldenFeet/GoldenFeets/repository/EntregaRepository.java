package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    List<Entrega> findByDistribuidor_IdUsuario(Integer idUsuario);

    @Query("SELECT e FROM Entrega e LEFT JOIN FETCH e.venta v LEFT JOIN FETCH v.cliente c WHERE " +
            "(:estado IS NULL OR e.estado = :estado) AND " +
            "(:distribuidorId IS NULL OR e.distribuidor.idUsuario = :distribuidorId) AND " +
            "(:fechaInicio IS NULL OR e.fechaCreacion >= :fechaInicio) AND " +
            "(:fechaFin IS NULL OR e.fechaCreacion <= :fechaFin) AND " +
            "(:clienteEmail IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :clienteEmail, '%')))")
    List<Entrega> findByFiltros(@Param("estado") String estado,
                                @Param("distribuidorId") Integer distribuidorId,
                                @Param("fechaInicio") LocalDateTime fechaInicio,
                                @Param("fechaFin") LocalDateTime fechaFin,
                                @Param("clienteEmail") String clienteEmail);

    long countByEstado(String estado);
    long countByDistribuidor_IdUsuarioAndEstado(Integer idUsuario, String estado);
    long countByDistribuidor_IdUsuarioAndFechaEntrega(Integer idUsuario, LocalDate fecha);
    long countByDistribuidor_IdUsuario(Integer idUsuario);

    // Método de conteo anterior (para estados activos)
    long countByDistribuidor_IdUsuarioAndEstadoInAndFechaCreacionBetween(
            Integer idUsuario,
            List<String> estados,
            LocalDateTime inicioDelDia,
            LocalDateTime finDelDia
    );

    // --- NUEVA CONSULTA (CORRECTA) ---
    /**
     * Cuenta todas las entregas que fueron ASIGNADAS a un distribuidor
     * en el rango de fechas actual (hoy).
     */
    long countByDistribuidor_IdUsuarioAndFechaAsignacionBetween(
            Integer idUsuario,
            LocalDateTime inicioDelDia,
            LocalDateTime finDelDia
    );
}