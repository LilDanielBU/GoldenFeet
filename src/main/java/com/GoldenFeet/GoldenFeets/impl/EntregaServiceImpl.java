package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.EstadisticasDTO;
import com.GoldenFeet.GoldenFeets.dto.EstadisticasDistribuidorDTO;
import com.GoldenFeet.GoldenFeets.dto.DistribuidorConteoDTO;
import com.GoldenFeet.GoldenFeets.entity.Entrega;
import com.GoldenFeet.GoldenFeets.entity.Novedad;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.repository.EntregaRepository;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import com.GoldenFeet.GoldenFeets.service.EmailService;
import com.GoldenFeet.GoldenFeets.service.EntregaService;
import com.GoldenFeet.GoldenFeets.service.NovedadService;
import com.GoldenFeet.GoldenFeets.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntregaServiceImpl implements EntregaService {

    private final EntregaRepository entregaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final UsuarioService usuarioService;
    private final NovedadService novedadService;

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

        String localidadEntrega = entrega.getLocalidad();
        String localidadDistribuidor = distribuidor.getLocalidad();

        if (localidadDistribuidor == null || localidadEntrega == null || !localidadDistribuidor.equalsIgnoreCase(localidadEntrega)) {
            throw new IllegalStateException(
                    "Error: El distribuidor (" + distribuidor.getNombre() +
                            ") no está asignado a la localidad de esta entrega (" + localidadEntrega + ")."
            );
        }

        LocalDateTime inicioDelDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDelDia = LocalDate.now().atTime(LocalTime.MAX);

        long entregasAsignadasHoy = entregaRepository.countByDistribuidor_IdUsuarioAndFechaAsignacionBetween(
                distribuidorId, inicioDelDia, finDelDia
        );

        if (entregasAsignadasHoy >= 15) {
            throw new IllegalStateException("El distribuidor ya alcanzó el límite de 15 entregas asignadas para hoy.");
        }

        entrega.setDistribuidor(distribuidor);
        entrega.setEstado("ASIGNADO");
        entrega.setFechaAsignacion(LocalDateTime.now());

        // Registramos quien asignó (en este caso el sistema o gerente, aquí pasamos null o podríamos pasar el usuario si lo tuviéramos)
        crearNovedad(entrega, null, "Entrega asignada al distribuidor: " + distribuidor.getNombre());

        entregaRepository.save(entrega);
    }

    @Override
    @Transactional
    // --- MÉTODO ACTUALIZADO ---
    public void cancelarEntrega(Long entregaId, String motivo, Usuario usuarioQueCancela) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new EntityNotFoundException("Entrega no encontrada con ID: " + entregaId));

        // Registramos la novedad con el usuario real
        String nombreUsuario = (usuarioQueCancela != null) ? usuarioQueCancela.getNombre() : "Sistema/Gerente";
        crearNovedad(entrega, usuarioQueCancela, "Entrega CANCELADA por " + nombreUsuario + ". Motivo: " + motivo);

        entrega.setEstado("CANCELADA");
        entrega.setMotivoCancelacion(motivo);
        entrega.setDistribuidor(null);
        entrega.setFechaAsignacion(null);
        entregaRepository.save(entrega);
    }

    @Override
    @Transactional
    public void desasignarDistribuidor(Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new EntityNotFoundException("Entrega no encontrada con ID: " + entregaId));

        Usuario distribuidorAnterior = entrega.getDistribuidor();
        crearNovedad(entrega, null, "Distribuidor desasignado: " + (distribuidorAnterior != null ? distribuidorAnterior.getNombre() : "N/A"));

        entrega.setDistribuidor(null);
        entrega.setEstado("PENDIENTE");
        entrega.setFechaAsignacion(null);
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

        String estadoAnterior = entrega.getEstado();
        entrega.setEstado(nuevoEstado);

        crearNovedad(entrega, entrega.getDistribuidor(), "Estado actualizado de '" + estadoAnterior + "' a '" + nuevoEstado + "'.");

        if ("ENTREGADO".equals(nuevoEstado)) {
            entrega.setFechaEntrega(LocalDate.now());
        }

        Entrega entregaGuardada = entregaRepository.save(entrega);

        if ("ENTREGADO".equals(nuevoEstado)) {
            emailService.enviarCorreoDeEntregaCompletada(entregaGuardada);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DistribuidorConteoDTO> obtenerDistribuidoresConConteo() {
        List<Usuario> distribuidores = usuarioService.findByRol("ROLE_DISTRIBUIDOR");
        LocalDateTime inicioDelDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDelDia = LocalDate.now().atTime(LocalTime.MAX);

        return distribuidores.stream().map(dist -> {
            long conteo = entregaRepository.countByDistribuidor_IdUsuarioAndFechaAsignacionBetween(
                    dist.getIdUsuario(),
                    inicioDelDia,
                    finDelDia
            );
            return new DistribuidorConteoDTO(
                    dist.getIdUsuario(),
                    dist.getNombre(),
                    conteo,
                    dist.getLocalidad()
            );
        }).collect(Collectors.toList());
    }

    private void crearNovedad(Entrega entrega, Usuario usuarioReporta, String descripcion) {
        Novedad novedad = new Novedad();
        novedad.setEntrega(entrega);
        novedad.setUsuarioReporta(usuarioReporta);
        novedad.setDescripcion(descripcion);
        novedad.setFecha(LocalDateTime.now());
        novedadService.guardar(novedad);
    }
}