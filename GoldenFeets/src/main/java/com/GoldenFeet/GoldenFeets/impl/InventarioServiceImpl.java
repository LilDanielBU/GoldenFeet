package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.entity.Inventario;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.exceptions.ResourceNotFoundException;
import com.GoldenFeet.GoldenFeets.repository.InventarioRepository;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InventarioServiceImpl implements InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoRepository productoRepository; // Necesario para crear nuevas entradas de inventario

    @Override
    public List<Inventario> findAllInventarios() {
        return inventarioRepository.findAll();
    }

    @Override
    public Optional<Inventario> findInventarioById(Long id) {
        return inventarioRepository.findById(id);
    }

    @Override
    @Transactional
    public Inventario saveInventario(Inventario inventario) {
        inventario.setUltimaActualizacion(LocalDateTime.now());
        return inventarioRepository.save(inventario);
    }

    @Override
    @Transactional
    public void deleteInventario(Long id) {
        inventarioRepository.deleteById(id);
    }

    @Override
    public Optional<Inventario> findByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }

    @Override
    @Transactional
    public Inventario actualizarStock(Long productoId, int cantidad, String operacion) {
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado para el producto ID: " + productoId));

        if ("sumar".equalsIgnoreCase(operacion)) {
            inventario.aumentarStock(cantidad);
        } else if ("restar".equalsIgnoreCase(operacion)) {
            inventario.reducirStock(cantidad);
        } else {
            throw new IllegalArgumentException("Operación de stock inválida: " + operacion);
        }
        return saveInventario(inventario); // Guarda el inventario actualizado
    }

    @Override
    @Transactional
    public Inventario crearInventarioInicial(Producto producto, int stockInicial) {
        if (inventarioRepository.findByProductoId(Long.valueOf(producto.getId())).isPresent()) {
            throw new IllegalArgumentException("Ya existe una entrada de inventario para el producto: " + producto.getNombre());
        }
        Inventario nuevoInventario = new Inventario(producto, stockInicial);
        return saveInventario(nuevoInventario);
    }
}