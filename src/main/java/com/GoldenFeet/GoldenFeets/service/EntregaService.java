package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.dto.DistribuidorConteoDTO;
import com.GoldenFeet.GoldenFeets.dto.EstadisticasDTO;
import com.GoldenFeet.GoldenFeets.dto.EstadisticasDistribuidorDTO;
import com.GoldenFeet.GoldenFeets.entity.Entrega;
import com.GoldenFeet.GoldenFeets.entity.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EntregaService {

    Entrega guardar(Entrega entrega);

    List<Entrega> findAll();

    Optional<Entrega> findById(Long id);

    void asignarDistribuidor(Long entregaId, Integer distribuidorId);

    // --- CAMBIO AQUÍ: Agregamos el parámetro Usuario ---
    void cancelarEntrega(Long entregaId, String motivo, Usuario usuarioQueCancela);

    void desasignarDistribuidor(Long entregaId);

    List<Entrega> buscarConFiltros(String estado, Integer distribuidorId, LocalDateTime fechaInicio, LocalDateTime fechaFin, String clienteEmail);

    List<Entrega> obtenerEntregasPorDistribuidor(Integer idDistribuidor);

    EstadisticasDTO obtenerEstadisticas();

    EstadisticasDistribuidorDTO obtenerEstadisticasDistribuidor(Integer idDistribuidor);

    void actualizarEstado(Long entregaId, String nuevoEstado);

    List<DistribuidorConteoDTO> obtenerDistribuidoresConConteo();
}