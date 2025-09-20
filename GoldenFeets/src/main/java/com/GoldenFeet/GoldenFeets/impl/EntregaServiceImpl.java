package com.GoldenFeet.GoldenFeets.service.impl;

import com.GoldenFeet.GoldenFeets.entity.Entrega;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.repository.EntregaRepository;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import com.GoldenFeet.GoldenFeets.service.EntregaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EntregaServiceImpl implements EntregaService {

    @Autowired
    private EntregaRepository entregaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<Entrega> findAll() {
        return entregaRepository.findAll();
    }

    @Override
    public Optional<Entrega> findById(Long id) {
        return entregaRepository.findById(id);
    }

    @Override
    @Transactional
    public void asignarDistribuidor(Long entregaId, Integer distribuidorId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega no encontrada con id: " + entregaId));

        Usuario distribuidor = usuarioRepository.findById(distribuidorId)
                .orElseThrow(() -> new RuntimeException("Distribuidor no encontrado con id: " + distribuidorId));

        entrega.setDistribuidor(distribuidor);
        entrega.setEstado("ASIGNADO");

        entregaRepository.save(entrega);
    }

    @Override
    @Transactional
    public void cancelarEntrega(Long entregaId, String motivo) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega no encontrada con id: " + entregaId));

        if ("ENTREGADO".equals(entrega.getEstado())) {
            throw new IllegalStateException("No se puede cancelar una entrega que ya fue completada.");
        }

        entrega.setEstado("CANCELADO");
        entrega.setMotivoCancelacion(motivo);
        entregaRepository.save(entrega);
    }

    @Override
    @Transactional
    public void desasignarDistribuidor(Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega no encontrada con id: " + entregaId));

        entrega.setDistribuidor(null);
        entrega.setEstado("PENDIENTE");

        entregaRepository.save(entrega);
    }
}