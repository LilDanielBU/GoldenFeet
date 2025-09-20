package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.entity.Entrega;
import java.util.List;
import java.util.Optional;

public interface EntregaService {

    List<Entrega> findAll();
    Optional<Entrega> findById(Long id);
    void asignarDistribuidor(Long entregaId, Integer distribuidorId);
    void cancelarEntrega(Long entregaId, String motivo);

    // Método nuevo
    void desasignarDistribuidor(Long entregaId);
}