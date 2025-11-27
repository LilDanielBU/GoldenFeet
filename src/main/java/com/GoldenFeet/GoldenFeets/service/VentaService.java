package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.CrearVentaRequestDTO;
import com.GoldenFeet.GoldenFeets.dto.VentaResponseDTO;
import com.GoldenFeet.GoldenFeets.entity.Venta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // Mantener Optional, ya que es una buena práctica para métodos de búsqueda por ID que pueden fallar.

public interface VentaService {

    // =========================================================
    // CRUD BÁSICO DE VENTA
    // =========================================================

    /** Crea una nueva venta a partir de la solicitud DTO y el email del cliente. */
    VentaResponseDTO crearVenta(CrearVentaRequestDTO request, String clienteEmail);

    /** Guarda una entidad Venta existente o nueva. */
    Venta guardarVenta(Venta venta);

    /** Elimina una venta por su ID. */
    void eliminarVenta(Long id);


    // =========================================================
    // MÉTODOS DE BÚSQUEDA (REPORTES/CONSULTA)
    // =========================================================

    /** Busca una venta específica por su ID. */
    Venta obtenerVentaPorId(Long id);

    /** Obtiene todas las ventas, retornando la entidad Venta completa. */
    List<Venta> obtenerTodasLasVentas();

    /** Obtiene todas las ventas, retornando la entidad Venta en formato DTO.
     * Mantener este método si es necesario para el controlador. */
    List<VentaResponseDTO> findAllVentas();

    /** Busca una venta específica por su ID, retornando un Optional.
     * Se mantiene si hay otra parte del código que lo usa. */
    Optional<Venta> findVentaById(Long id);


    /** Obtiene las ventas en un rango de fechas. */
    List<Venta> obtenerVentasPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin);

    long contarVentas();

    double obtenerTotalIngresos();

    List<VentaResponseDTO> buscarVentasPorCliente(Integer idCliente);

    /** Busca ventas por ID de cliente, usando Long. */
    List<VentaResponseDTO> buscarVentasPorCliente(Long idCliente);

    long contarVentasPendientes();
}