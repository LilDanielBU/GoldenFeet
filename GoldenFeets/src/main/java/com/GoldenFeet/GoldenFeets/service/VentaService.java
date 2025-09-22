package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.CrearVentaRequestDTO;
import com.GoldenFeet.GoldenFeets.dto.VentaResponseDTO;
import java.util.List;
import com.GoldenFeet.GoldenFeets.entity.Venta;
import java.time.LocalDate;

public interface VentaService {


    VentaResponseDTO crearVenta(CrearVentaRequestDTO request, String clienteEmail);

    List<VentaResponseDTO> buscarVentasPorCliente(Integer idCliente);
    List<Venta> obtenerTodasLasVentas();
    List<Venta> obtenerVentasPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin);
    Venta obtenerVentaPorId(Long id);
    Venta guardarVenta(Venta venta);
    void eliminarVenta(Long id);
}