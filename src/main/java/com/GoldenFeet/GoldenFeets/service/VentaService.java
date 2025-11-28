package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.CrearVentaRequestDTO;
import com.GoldenFeet.GoldenFeets.dto.VentaResponseDTO;
import com.GoldenFeet.GoldenFeets.entity.Venta;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VentaService {

    // =========================================================
    // CRUD BÁSICO DE VENTA
    // =========================================================

    VentaResponseDTO crearVenta(CrearVentaRequestDTO request, String clienteEmail);

    Venta guardarVenta(Venta venta);

    void eliminarVenta(Long id);


    // =========================================================
    // MÉTODOS DE BÚSQUEDA (REPORTES/CONSULTA)
    // =========================================================

    Venta obtenerVentaPorId(Long id);

    List<Venta> obtenerTodasLasVentas();

    List<VentaResponseDTO> findAllVentas();

    Optional<Venta> findVentaById(Long id);

    List<Venta> obtenerVentasPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin);

    long contarVentas();

    double obtenerTotalIngresos();

    List<VentaResponseDTO> buscarVentasPorCliente(Integer idCliente);

    List<VentaResponseDTO> buscarVentasPorCliente(Long idCliente);

    long contarVentasPendientes();


    // =========================================================
    // MÉTODOS PARA DASHBOARD ADMIN (ESTADÍSTICAS)
    // =========================================================

    /** Total de ventas (dinero) del mes actual */
    double obtenerVentasDelMes();

    /** Unidades vendidas en el mes actual */
    int obtenerUnidadesVendidasMes();

    /** Ticket promedio del mes (total ventas / número de ventas) */
    double obtenerTicketPromedioMes();

    /** Ventas de los últimos 6 meses (para gráfica) */
    Map<String, Double> obtenerVentasUltimosMeses();
}
