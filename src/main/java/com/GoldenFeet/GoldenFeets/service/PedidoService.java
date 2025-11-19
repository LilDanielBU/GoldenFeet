package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.PedidoRequestDTO;
import com.GoldenFeet.GoldenFeets.entity.Venta;

public interface PedidoService {
    /**
     * Crea una nueva venta (pedido) en la base de datos.
     * @param pedidoRequest DTO con los datos del carrito y del cliente.
     * @return La entidad Venta que fue guardada.
     */
    Venta crearPedido(PedidoRequestDTO pedidoRequest);
}