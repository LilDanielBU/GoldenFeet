package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.CrearVentaRequestDTO;
import com.GoldenFeet.GoldenFeets.dto.VentaResponseDTO;
import com.GoldenFeet.GoldenFeets.entity.Venta;

import java.util.List;
import java.util.Optional;

public interface VentaService {

    /**
     * Crea una nueva venta, procesa los detalles y actualiza el inventario.
     * @param request DTO con la información del pedido.
     * @param clienteEmail Email del usuario que realiza la compra.
     * @return DTO con la información de la venta creada.
     */
    VentaResponseDTO crearVenta(CrearVentaRequestDTO request, String clienteEmail);

    /**
     * Busca todas las ventas realizadas por un cliente específico.
     * @param idCliente ID del cliente.
     * @return Una lista de DTOs de las ventas del cliente.
     */
    List<VentaResponseDTO> buscarVentasPorCliente(Long idCliente);

    /**
     * Obtiene todas las ventas del sistema. Necesario para el panel de administración.
     * @return Una lista de DTOs de todas las ventas.
     */
    List<VentaResponseDTO> findAllVentas();

    /**
     * Busca una venta específica por su ID.
     *
     * @param id El ID de la venta.
     * @return Un Optional con el DTO de la venta si se encuentra.
     */
    Optional<Venta> findVentaById(Long id);
}