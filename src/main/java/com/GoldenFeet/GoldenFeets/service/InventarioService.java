package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.entity.Inventario;
import com.GoldenFeet.GoldenFeets.entity.Producto;

import java.util.List;
import java.util.Optional;

public interface InventarioService {

    List<Inventario> findAllInventarios();

    Optional<Inventario> findInventarioById(Long id);

    Inventario saveInventario(Inventario inventario);

    void deleteInventario(Long id);

    // --- CORRECCIÓN: Se cambió el parámetro de Long a Integer ---
    Optional<Inventario> findByProductoId(Integer productoId);

    // --- CORRECCIÓN: Se cambió el parámetro de Long a Integer ---
    Inventario actualizarStock(Integer productoId, int cantidad, String operacion);

    Inventario crearInventarioInicial(Producto producto, int stockInicial);
}