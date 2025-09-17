package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.CrearVentaRequestDTO;
import com.GoldenFeet.GoldenFeets.dto.VentaResponseDTO;
import java.util.List;

public interface VentaService {

    /**
     * Procesa y crea una nueva venta (pedido).
     * @param request DTO con el id del cliente y la lista de productos a comprar.
     * @return un DTO con el resumen de la venta creada.
     */
    VentaResponseDTO crearVenta(CrearVentaRequestDTO request);

    /**
     * Busca y devuelve todas las ventas de un cliente específico.
     * @param idCliente el ID del usuario cliente.
     * @return una lista de DTOs con el resumen de cada venta.
     */
    List<VentaResponseDTO> buscarVentasPorCliente(Integer idCliente);
}