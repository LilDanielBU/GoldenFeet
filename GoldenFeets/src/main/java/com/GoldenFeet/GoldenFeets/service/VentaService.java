package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.CrearVentaRequestDTO;
import com.GoldenFeet.GoldenFeets.dto.VentaResponseDTO;
import java.util.List;

public interface VentaService {


    VentaResponseDTO crearVenta(CrearVentaRequestDTO request, String clienteEmail);

    List<VentaResponseDTO> buscarVentasPorCliente(Integer idCliente);
}