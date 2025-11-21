package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.PedidoRequestDTO;
import com.GoldenFeet.GoldenFeets.entity.DetalleVenta;
import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.entity.Venta;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.entity.Entrega;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.repository.VentaRepository;
import com.GoldenFeet.GoldenFeets.repository.InventarioMovimientoRepository;
import com.GoldenFeet.GoldenFeets.repository.EntregaRepository;
import com.GoldenFeet.GoldenFeets.service.PedidoService;
import com.GoldenFeet.GoldenFeets.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioMovimientoRepository inventarioMovimientoRepository;
    private final UsuarioService usuarioService;
    private final EntregaRepository entregaRepository;

    @Override
    @Transactional
    public Venta crearPedido(PedidoRequestDTO pedidoRequest) {

        // 1. Convertir IDs de Producto (Integer del DTO) a List<Long>
        List<Long> productoIdsLong = pedidoRequest.getItems().stream()
                .map(item -> item.getProductoId().longValue())
                .collect(Collectors.toList());

        // Mapa de productos para búsqueda rápida
        Map<Long, Producto> productosMap = productoRepository.findAllById(productoIdsLong).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));


        Venta nuevaVenta = new Venta();
        nuevaVenta.setFechaVenta(LocalDate.now());
        nuevaVenta.setEstado("PROCESANDO");

        // 2. Asignar el cliente
        Usuario cliente = usuarioService.buscarPorId(pedidoRequest.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + pedidoRequest.getClienteId()));

        nuevaVenta.setCliente(cliente);

        // 3. Inicializar campos de Venta
        String localidadVenta = pedidoRequest.getLocalidad() != null ? pedidoRequest.getLocalidad() : cliente.getLocalidad();

        nuevaVenta.setDireccionEnvio(pedidoRequest.getDireccionEnvio() != null ? pedidoRequest.getDireccionEnvio() : cliente.getDireccion());
        nuevaVenta.setCiudadEnvio(pedidoRequest.getCiudadEnvio() != null ? pedidoRequest.getCiudadEnvio() : cliente.getCiudad());
        nuevaVenta.setLocalidad(localidadVenta);
        nuevaVenta.setMetodoPago(pedidoRequest.getMetodoPago() != null ? pedidoRequest.getMetodoPago() : "PENDIENTE_PAGO");
        nuevaVenta.setIdTransaccion(pedidoRequest.getIdTransaccion() != null ? pedidoRequest.getIdTransaccion() : "N/A-" + UUID.randomUUID().toString());

        Set<DetalleVenta> detalles = new HashSet<>();
        BigDecimal totalPedido = BigDecimal.ZERO;

        for (var itemDTO : pedidoRequest.getItems()) {

            // Usamos Long para buscar en el mapa
            Producto producto = productosMap.get(itemDTO.getProductoId().longValue());
            if (producto == null) throw new RuntimeException("Producto no encontrado con ID: " + itemDTO.getProductoId());

            int stockActual = producto.getStock() != null ? producto.getStock() : 0;
            int cantidadPedida = itemDTO.getCantidad();

            if (stockActual < cantidadPedida) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() + ". Stock actual: " + stockActual);
            }

            // 4. REDUCCIÓN DE STOCK
            producto.setStock(stockActual - cantidadPedida);
            productoRepository.save(producto);

            // 5. REGISTRO DE MOVIMIENTO
            InventarioMovimiento movimiento = new InventarioMovimiento();
            movimiento.setProducto(producto);
            movimiento.setTipoMovimiento("VENTA");
            movimiento.setCantidad(cantidadPedida);
            movimiento.setMotivo("Venta de producto registrada.");
            movimiento.setFecha(LocalDateTime.now());
            inventarioMovimientoRepository.save(movimiento);

            // 6. CREACIÓN DE DETALLE
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidadPedida);

            // 💥 CORRECCIÓN 1: Convertir Double a BigDecimal
            BigDecimal precioUnitario = BigDecimal.valueOf(producto.getPrecio());
            detalle.setPrecioUnitario(precioUnitario);

            // 💥 CORRECCIÓN 2: Usar el BigDecimal convertido para multiplicar
            // (Double no tiene método .multiply())
            BigDecimal subtotal = precioUnitario.multiply(new BigDecimal(cantidadPedida));
            detalle.setSubtotal(subtotal);

            detalle.setVenta(nuevaVenta);

            detalles.add(detalle);
            totalPedido = totalPedido.add(detalle.getSubtotal());
        }

        nuevaVenta.setDetallesVenta(new ArrayList<>(detalles));
        nuevaVenta.setTotal(totalPedido);

        // 7. Guardamos la Venta
        Venta ventaGuardada = ventaRepository.save(nuevaVenta);

        // 8. Creación EXPLÍCITA de Entrega
        Entrega nuevaEntrega = new Entrega();
        nuevaEntrega.setVenta(ventaGuardada);
        nuevaEntrega.setLocalidad(ventaGuardada.getLocalidad());

        // 9. Guardamos la Entrega.
        entregaRepository.save(nuevaEntrega);

        return ventaGuardada;
    }
}