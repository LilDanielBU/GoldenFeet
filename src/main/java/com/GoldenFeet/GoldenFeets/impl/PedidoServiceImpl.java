package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.ItemPedidoDTO; // Importar el DTO de item de pedido (asumo este nombre)
import com.GoldenFeet.GoldenFeets.dto.PedidoRequestDTO;
import com.GoldenFeet.GoldenFeets.entity.DetalleVenta;
import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import com.GoldenFeet.GoldenFeets.entity.VarianteProducto; // NUEVO IMPORT
import com.GoldenFeet.GoldenFeets.entity.Venta;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.entity.Entrega;
// Quitamos ProductoRepository, ya que no se usa para stock
import com.GoldenFeet.GoldenFeets.repository.VentaRepository;
import com.GoldenFeet.GoldenFeets.repository.InventarioMovimientoRepository;
import com.GoldenFeet.GoldenFeets.repository.EntregaRepository;
import com.GoldenFeet.GoldenFeets.repository.VarianteProductoRepository; // NUEVO REPOSITORIO
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final VentaRepository ventaRepository;
    private final InventarioMovimientoRepository inventarioMovimientoRepository;
    private final UsuarioService usuarioService;
    private final EntregaRepository entregaRepository;
    private final VarianteProductoRepository varianteRepository; // NUEVA INYECCIÓN

    @Override
    @Transactional
    public Venta crearPedido(PedidoRequestDTO pedidoRequest) {

        // 1. Asignar el cliente
        Usuario cliente = usuarioService.buscarPorId(pedidoRequest.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + pedidoRequest.getClienteId()));

        Venta nuevaVenta = new Venta();
        nuevaVenta.setCliente(cliente);
        nuevaVenta.setFechaVenta(LocalDate.now());
        nuevaVenta.setEstado("PROCESANDO");

        // 2. Inicializar campos de Venta con datos de envío
        String localidadVenta = pedidoRequest.getLocalidad() != null ? pedidoRequest.getLocalidad() : cliente.getLocalidad();

        nuevaVenta.setDireccionEnvio(pedidoRequest.getDireccionEnvio() != null ? pedidoRequest.getDireccionEnvio() : cliente.getDireccion());
        nuevaVenta.setCiudadEnvio(pedidoRequest.getCiudadEnvio() != null ? pedidoRequest.getCiudadEnvio() : cliente.getCiudad());
        nuevaVenta.setLocalidad(localidadVenta);
        nuevaVenta.setMetodoPago(pedidoRequest.getMetodoPago() != null ? pedidoRequest.getMetodoPago() : "PENDIENTE_PAGO");
        nuevaVenta.setIdTransaccion(pedidoRequest.getIdTransaccion() != null ? pedidoRequest.getIdTransaccion() : "N/A-" + UUID.randomUUID().toString());

        Set<DetalleVenta> detalles = new HashSet<>();
        BigDecimal totalPedido = BigDecimal.ZERO;

        // 3. Procesar Ítems (Usando Variantes)
        for (ItemPedidoDTO itemDTO : pedidoRequest.getItems()) {

            // CRÍTICO: Obtenemos datos de la variante requerida
            Long productoId = itemDTO.getProductoId().longValue();

            // Asumiendo que getTalla() y getColor() devuelven el formato correcto (String)
            Integer tallaRequerida = Integer.parseInt(itemDTO.getTalla());
            String colorRequerido = itemDTO.getColor();
            int cantidadPedida = itemDTO.getCantidad();

            // Buscar la VARIANTE específica
            // CORRECCIÓN: Usamos findByProductoId (sin guion bajo)
            List<VarianteProducto> variantesDelProducto = varianteRepository.findByProductoId(productoId);

            VarianteProducto variante = variantesDelProducto.stream()
                    .filter(v -> v.getTalla().equals(tallaRequerida) && v.getColor().equalsIgnoreCase(colorRequerido))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Variante no encontrada: " + colorRequerido + " T" + tallaRequerida));

            // Verificamos el stock en la VARIANTE
            int stockActual = variante.getStock() != null ? variante.getStock() : 0;

            if (stockActual < cantidadPedida) {
                throw new RuntimeException("Stock insuficiente para: " + variante.getSku() + ". Stock actual: " + stockActual);
            }

            // 4. REDUCCIÓN DE STOCK en la VARIANTE
            variante.setStock(stockActual - cantidadPedida);
            varianteRepository.save(variante); // Guardamos la variante

            // 5. REGISTRO DE MOVIMIENTO
            InventarioMovimiento movimiento = new InventarioMovimiento();
            movimiento.setVariante(variante); // Seteamos la variante
            movimiento.setTipoMovimiento("VENTA");
            movimiento.setCantidad(cantidadPedida);
            movimiento.setMotivo("Venta de producto registrada.");
            movimiento.setFecha(LocalDateTime.now());
            inventarioMovimientoRepository.save(movimiento);

            // 6. CREACIÓN DE DETALLE
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVariante(variante); // Seteamos la variante
            detalle.setCantidad(cantidadPedida);

            // Obtenemos el precio del Producto Padre
            BigDecimal precioUnitario = BigDecimal.valueOf(variante.getProducto().getPrecio());
            detalle.setPrecioUnitario(precioUnitario);

            BigDecimal subtotal = precioUnitario.multiply(new BigDecimal(cantidadPedida));
            detalle.setSubtotal(subtotal);

            // Guardamos Talla y Color como datos históricos
            detalle.setTalla(itemDTO.getTalla());
            detalle.setColor(itemDTO.getColor());

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
        nuevaEntrega.setEstado("Pendiente");
        nuevaEntrega.setFechaCreacion(LocalDateTime.now());
        nuevaEntrega.setLocalidad(ventaGuardada.getLocalidad());

        // 9. Guardamos la Entrega.
        entregaRepository.save(nuevaEntrega);

        return ventaGuardada;
    }
}