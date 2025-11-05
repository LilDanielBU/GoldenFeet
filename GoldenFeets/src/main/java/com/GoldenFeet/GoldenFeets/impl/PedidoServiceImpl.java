package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.ItemPedidoDTO;
import com.GoldenFeet.GoldenFeets.dto.PedidoRequestDTO;
import com.GoldenFeet.GoldenFeets.entity.DetalleVenta;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.entity.Venta;
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import com.GoldenFeet.GoldenFeets.repository.VentaRepository;
import com.GoldenFeet.GoldenFeets.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    // private final UsuarioService usuarioService;

    @Override
    @Transactional
    public Venta crearPedido(PedidoRequestDTO pedidoRequest) {

        // --- INICIO DE CORRECCIÓN ---

        // 1. La lista de IDs es 'Integer' (coincide con ItemPedidoDTO.getProductoId)
        List<Integer> productoIds = pedidoRequest.getItems().stream()
                .map(ItemPedidoDTO::getProductoId)
                .collect(Collectors.toList());

        // 2. El mapa usa 'Integer' como clave.
        // 'findAllById' ahora recibe List<Integer>.
        // 'Producto::getId' ahora devuelve 'Integer', coincidiendo con la clave del mapa.
        Map<Integer, Producto> productosMap = productoRepository.findAllById(productoIds).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        // --- FIN DE CORRECCIÓN ---

        Venta nuevaVenta = new Venta();
        nuevaVenta.setFechaVenta(LocalDate.now());
        nuevaVenta.setEstado("PROCESANDO");
        // Usuario cliente = usuarioService.buscarPorId(pedidoRequest.getClienteId());
        // nuevaVenta.setCliente(cliente);

        Set<DetalleVenta> detalles = new HashSet<>();
        BigDecimal totalPedido = BigDecimal.ZERO;

        for (var itemDTO : pedidoRequest.getItems()) {

            // Buscamos en el mapa por 'Integer' (el ID del DTO)
            Producto producto = productosMap.get(itemDTO.getProductoId());

            if (producto == null) throw new RuntimeException("Producto no encontrado con ID: " + itemDTO.getProductoId());

            int stockActual = producto.getStock() != null ? producto.getStock() : 0;
            int cantidadPedida = itemDTO.getCantidad();

            if (stockActual < cantidadPedida) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }
            producto.setStock(stockActual - cantidadPedida);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidadPedida);
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio().multiply(new BigDecimal(cantidadPedida)));
            detalle.setVenta(nuevaVenta);

            detalles.add(detalle);
            totalPedido = totalPedido.add(detalle.getSubtotal());
        }

        nuevaVenta.setDetallesVenta(new ArrayList<>(detalles));
        nuevaVenta.setTotal(totalPedido);

        return ventaRepository.save(nuevaVenta);
    }
}