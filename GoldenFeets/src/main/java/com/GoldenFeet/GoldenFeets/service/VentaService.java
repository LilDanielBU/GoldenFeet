package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.CrearVentaRequestDTO;
import com.GoldenFeet.GoldenFeets.dto.VentaResponseDTO;
import com.GoldenFeet.GoldenFeets.entity.Venta;

import java.util.List;
import java.util.Optional;

public interface VentaService {
    List<Venta> findAllVentas();
    Optional<Venta> findVentaById(Long id);
    void deleteVenta(Long id);

    // MÉTODO ACTUALIZADO para usar tus DTOs y la nueva lógica de inventario
    VentaResponseDTO crearVenta(CrearVentaRequestDTO requestDTO, String clienteEmail);
}