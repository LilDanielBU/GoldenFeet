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
    Optional<Inventario> findByProductoId(Long productoId);
    Inventario actualizarStock(Long productoId, int cantidad, String operacion); // "sumar" o "restar"
    Inventario crearInventarioInicial(Producto producto, int stockInicial);
}