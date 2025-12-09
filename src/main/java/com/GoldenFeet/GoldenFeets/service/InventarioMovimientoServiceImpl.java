package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.HistorialDTO;
import com.GoldenFeet.GoldenFeets.dto.IngresoDTO;
import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import com.GoldenFeet.GoldenFeets.entity.VarianteProducto;
import com.GoldenFeet.GoldenFeets.repository.InventarioMovimientoRepository;
import com.GoldenFeet.GoldenFeets.repository.VarianteProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventarioMovimientoServiceImpl implements InventarioMovimientoService {

    // Nota: El nombre del campo es 'varianteRepository'
    private final InventarioMovimientoRepository inventarioMovimientoRepository;
    private final VarianteProductoRepository varianteRepository;

    @Autowired
    public InventarioMovimientoServiceImpl(InventarioMovimientoRepository inventarioMovimientoRepository,
                                           VarianteProductoRepository varianteRepository) {
        this.inventarioMovimientoRepository = inventarioMovimientoRepository;
        this.varianteRepository = varianteRepository;
    }

    @Override
    @Transactional
    public void registrarIngreso(IngresoDTO ingresoDTO) {
        if (ingresoDTO.getCantidad() == null || ingresoDTO.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        // CORRECCIÓN: Usamos getVarianteId()
        Long varianteId = ingresoDTO.getVarianteId();

        VarianteProducto variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new EntityNotFoundException("Variante no encontrada con ID: " + varianteId));

        int stockActual = variante.getStock() != null ? variante.getStock() : 0;
        variante.setStock(stockActual + ingresoDTO.getCantidad());
        varianteRepository.save(variante);

        InventarioMovimiento movimiento = new InventarioMovimiento(
                variante,
                "INGRESO",
                ingresoDTO.getCantidad(),
                ingresoDTO.getMotivo()
        );
        movimiento.setFecha(LocalDateTime.now()); // Asegurar fecha

        inventarioMovimientoRepository.save(movimiento);
    }

    @Override
    @Transactional
    public void registrarSalida(IngresoDTO salidaDTO) {
        if (salidaDTO.getCantidad() == null || salidaDTO.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        // CORRECCIÓN: Usamos getVarianteId()
        Long varianteId = salidaDTO.getVarianteId();

        VarianteProducto variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new EntityNotFoundException("Variante no encontrada con ID: " + varianteId));

        int stockActual = variante.getStock() != null ? variante.getStock() : 0;
        int cantidadRetirada = salidaDTO.getCantidad();
        int stockNuevo = stockActual - cantidadRetirada;

        if (stockNuevo < 0) {
            throw new IllegalArgumentException("Stock insuficiente. Actual: " + stockActual);
        }

        variante.setStock(stockNuevo);
        varianteRepository.save(variante);

        InventarioMovimiento movimiento = new InventarioMovimiento(
                variante,
                "SALIDA",
                cantidadRetirada,
                salidaDTO.getMotivo()
        );
        movimiento.setFecha(LocalDateTime.now());

        inventarioMovimientoRepository.save(movimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialDTO> getHistorialPorProducto(Integer varianteId) {
        // Usamos findByVariante_Id, que asume que el ID del variante es un Long en el repositorio
        List<InventarioMovimiento> movimientos = inventarioMovimientoRepository
                .findByVariante_Id(varianteId.longValue());

        return movimientos.stream().map(m -> new HistorialDTO(
                m.getFecha(),
                m.getTipoMovimiento(),
                m.getCantidad(),
                m.getMotivo()
        )).collect(Collectors.toList());
    }



}