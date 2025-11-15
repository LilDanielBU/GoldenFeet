package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.CrearVentaRequestDTO;
import com.GoldenFeet.GoldenFeets.dto.VentaResponseDTO;
import com.GoldenFeet.GoldenFeets.entity.Venta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VentaService {

    VentaResponseDTO crearVenta(CrearVentaRequestDTO request, String clienteEmail);

    // Métodos que tus compañeros añadieron
    List<VentaResponseDTO> buscarVentasPorCliente(Long idCliente);
    List<VentaResponseDTO> findAllVentas();
    Optional<Venta> findVentaById(Long id);

    // Métodos que ya tenías
    List<Venta> obtenerTodasLasVentas();
    List<Venta> obtenerVentasPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin);
    Venta obtenerVentaPorId(Long id);
    Venta guardarVenta(Venta venta);
    void eliminarVenta(Long id);

    // Método que tenías pero que no estaba en la interfaz (causaba error de @Override)
    List<VentaResponseDTO> buscarVentasPorCliente(Integer idCliente);

    long contarVentas();
    double obtenerTotalIngresos();
}