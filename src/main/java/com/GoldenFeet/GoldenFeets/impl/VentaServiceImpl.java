package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.*;
import com.GoldenFeet.GoldenFeets.entity.*;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import com.GoldenFeet.GoldenFeets.repository.VentaRepository;
import com.GoldenFeet.GoldenFeets.service.EntregaService;
import com.GoldenFeet.GoldenFeets.service.VentaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EntregaService entregaService;

    @Override
    @Transactional
    public VentaResponseDTO crearVenta(CrearVentaRequestDTO request, String clienteEmail) {
        Usuario cliente = usuarioRepository.findByEmail(clienteEmail)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con email: " + clienteEmail));

        Venta nuevaVenta = new Venta();
        nuevaVenta.setCliente(cliente);
        nuevaVenta.setFechaVenta(LocalDate.now());
        nuevaVenta.setEstado("En Camino");

        // --- GUARDANDO DATOS DE ENVÍO (INCLUYENDO LOCALIDAD) ---
        nuevaVenta.setDireccionEnvio(request.getDireccion());
        nuevaVenta.setCiudadEnvio(request.getCiudad() + ", " + request.getDepartamento());
        nuevaVenta.setMetodoPago(request.getMetodoPago());
        nuevaVenta.setLocalidad(request.getLocalidad());
        // --- FIN DE DATOS DE ENVÍO ---

        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal totalVenta = BigDecimal.ZERO;

        for (ItemVentaDTO itemDTO : request.getItems()) {
            // Aseguramos conversión a Long para el ID
            Producto producto = productoRepository.findById(Long.valueOf(itemDTO.productoId()))
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + itemDTO.productoId()));

            // 1. Verificamos si hay stock suficiente
            if (producto.getStock() < itemDTO.cantidad()) {
                throw new IllegalStateException("Stock insuficiente para: " + producto.getNombre());
            }

            // 💥 CORRECCIÓN IMPORTANTE: Descontar stock y guardar el producto
            int nuevoStock = producto.getStock() - itemDTO.cantidad();
            producto.setStock(nuevoStock);
            productoRepository.save(producto); // <--- ESTO FALTABA
            // -----------------------------------------------------------

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(itemDTO.cantidad());

            // Convertir Double a BigDecimal de forma segura
            BigDecimal precioUnitario = BigDecimal.valueOf(producto.getPrecio());
            detalle.setPrecioUnitario(precioUnitario);

            // Usar el BigDecimal convertido para multiplicar
            BigDecimal subtotal = precioUnitario.multiply(new BigDecimal(itemDTO.cantidad()));

            detalle.setSubtotal(subtotal);
            detalle.setVenta(nuevaVenta);
            detalles.add(detalle);
            totalVenta = totalVenta.add(subtotal);
        }

        nuevaVenta.setTotal(totalVenta);
        nuevaVenta.setDetallesVenta(detalles);
        Venta ventaGuardada = ventaRepository.save(nuevaVenta);

        // --- CREANDO LA ENTREGA (CON LOCALIDAD) ---
        Entrega nuevaEntrega = new Entrega();
        nuevaEntrega.setVenta(ventaGuardada);
        nuevaEntrega.setEstado("PENDIENTE");
        nuevaEntrega.setFechaCreacion(LocalDateTime.now());
        nuevaEntrega.setLocalidad(ventaGuardada.getLocalidad());
        entregaService.guardar(nuevaEntrega);
        // --- FIN DE CREACIÓN DE ENTREGA ---

        return convertirAVentaResponseDTO(ventaGuardada);
    }

    @Override
    public long contarVentas() {
        return ventaRepository.count();
    }

    @Override
    public double obtenerTotalIngresos() {
        Double total = ventaRepository.sumarTotalVentas();
        return total != null ? total : 0.0;
    }

    @Override
    public long contarVentasPendientes() {
        return ventaRepository.countByEstado("PENDIENTE");
    }


    @Override
    public List<VentaResponseDTO> buscarVentasPorCliente(Integer idCliente) {
        return ventaRepository.findByCliente_IdUsuario(idCliente).stream()
                .map(this::convertirAVentaResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VentaResponseDTO> buscarVentasPorCliente(Long idCliente) {
        return buscarVentasPorCliente(idCliente.intValue());
    }

    @Override
    public List<VentaResponseDTO> findAllVentas() {
        return ventaRepository.findAll().stream()
                .map(this::convertirAVentaResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Venta> findVentaById(Long id) {
        return ventaRepository.findById(id);
    }

    @Override
    public List<Venta> obtenerTodasLasVentas() {
        return ventaRepository.findAll();
    }

    @Override
    public List<Venta> obtenerVentasPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        return ventaRepository.findByFechaVentaBetween(fechaInicio, fechaFin);
    }

    @Override
    public double obtenerVentasDelMes() {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now();

        Double total = ventaRepository.sumarVentasPorRango(inicioMes, finMes);
        return total != null ? total : 0.0;
    }

    @Override
    public int obtenerUnidadesVendidasMes() {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now();

        Integer unidades = ventaRepository.contarUnidadesVendidasRango(inicioMes, finMes);
        return unidades != null ? unidades : 0;
    }

    @Override
    public double obtenerTicketPromedioMes() {
        long cantidadVentas = this.contarVentas();
        double totalMes = this.obtenerVentasDelMes();

        if (cantidadVentas == 0) return 0.0;

        return totalMes / cantidadVentas;
    }

    @Override
    public Map<String, Double> obtenerVentasUltimosMeses() {
        LocalDate hoy = LocalDate.now();
        Map<String, Double> ventas = new LinkedHashMap<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate inicio = hoy.minusMonths(i).withDayOfMonth(1);
            LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());

            Double totalMes = ventaRepository.sumarVentasPorRango(inicio, fin);
            ventas.put(inicio.getMonth().toString(), totalMes != null ? totalMes : 0.0);
        }

        return ventas;
    }


    @Override
    public Venta obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Venta guardarVenta(Venta venta) {
        return ventaRepository.save(venta);
    }

    @Override
    @Transactional
    public void eliminarVenta(Long id) {
        if (!ventaRepository.existsById(id)) {
            throw new EntityNotFoundException("Venta no encontrada con ID: " + id);
        }
        ventaRepository.deleteById(id);
    }

    private VentaResponseDTO convertirAVentaResponseDTO(Venta venta) {
        return VentaResponseDTO.fromEntity(venta);
    }
}