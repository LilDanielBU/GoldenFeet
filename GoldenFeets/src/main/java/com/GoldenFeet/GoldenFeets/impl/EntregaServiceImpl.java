package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.EstadisticasDTO;
import com.GoldenFeet.GoldenFeets.dto.EstadisticasDistribuidorDTO;
import com.GoldenFeet.GoldenFeets.entity.Entrega;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.repository.EntregaRepository;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import com.GoldenFeet.GoldenFeets.service.EmailService;
import com.GoldenFeet.GoldenFeets.service.EntregaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EntregaServiceImpl implements EntregaService {

    private final EntregaRepository entregaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public Entrega guardar(Entrega entrega) {
        return entregaRepository.save(entrega);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Entrega> findAll() {
        return entregaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Entrega> findById(Long id) {
        return entregaRepository.findById(id);
    }

    @Override
    @Transactional
    public void asignarDistribuidor(Long entregaId, Integer distribuidorId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new EntityNotFoundException("Entrega no encontrada con ID: " + entregaId));
        Usuario distribuidor = usuarioRepository.findById(distribuidorId)
                .orElseThrow(() -> new EntityNotFoundException("Distribuidor no encontrado con ID: " + distribuidorId));

        entrega.setDistribuidor(distribuidor);
        entrega.setEstado("ASIGNADO");
        entregaRepository.save(entrega);
    }

    @Override
    @Transactional
    public void cancelarEntrega(Long entregaId, String motivo) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new EntityNotFoundException("Entrega no encontrada con ID: " + entregaId));
        entrega.setEstado("CANCELADA");
        entrega.setMotivoCancelacion(motivo);
        entrega.setDistribuidor(null);
        entregaRepository.save(entrega);
    }

    @Override
    @Transactional
    public void desasignarDistribuidor(Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new EntityNotFoundException("Entrega no encontrada con ID: " + entregaId));
        entrega.setDistribuidor(null);
        entrega.setEstado("PENDIENTE");
        entregaRepository.save(entrega);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Entrega> buscarConFiltros(String estado, Integer distribuidorId, LocalDateTime fechaInicio, LocalDateTime fechaFin, String clienteEmail) {
        String estadoFinal = (estado != null && estado.isEmpty()) ? null : estado;
        String emailFinal = (clienteEmail != null && clienteEmail.isEmpty()) ? null : clienteEmail;
        return entregaRepository.findByFiltros(estadoFinal, distribuidorId, fechaInicio, fechaFin, emailFinal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Entrega> obtenerEntregasPorDistribuidor(Integer idDistribuidor) {
        return entregaRepository.findByDistribuidor_IdUsuario(idDistribuidor);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasDTO obtenerEstadisticas() {
        long total = entregaRepository.count();
        long pendientes = entregaRepository.countByEstado("PENDIENTE");
        long enCamino = entregaRepository.countByEstado("EN CAMINO");
        long completadas = entregaRepository.countByEstado("ENTREGADO");
        return new EstadisticasDTO(total, pendientes, enCamino, completadas);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasDistribuidorDTO obtenerEstadisticasDistribuidor(Integer idDistribuidor) {
        long total = entregaRepository.countByDistribuidor_IdUsuario(idDistribuidor);
        long enCamino = entregaRepository.countByDistribuidor_IdUsuarioAndEstado(idDistribuidor, "EN CAMINO");
        long completadasHoy = entregaRepository.countByDistribuidor_IdUsuarioAndFechaEntrega(idDistribuidor, LocalDate.now());

        return new EstadisticasDistribuidorDTO(total, enCamino, completadasHoy);
    }

    @Override
    @Transactional
    public void actualizarEstado(Long entregaId, String nuevoEstado) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new EntityNotFoundException("Entrega no encontrada con ID: " + entregaId));

        entrega.setEstado(nuevoEstado);

        if ("ENTREGADO".equals(nuevoEstado)) {
            entrega.setFechaEntrega(LocalDate.now());
        }

        Entrega entregaGuardada = entregaRepository.save(entrega);

        if ("ENTREGADO".equals(nuevoEstado)) {
            emailService.enviarCorreoDeEntregaCompletada(entregaGuardada);
        }
    }
}