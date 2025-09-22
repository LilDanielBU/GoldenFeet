package com.GoldenFeet.GoldenFeets.impl;

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
    // Si tienes un servicio de usuarios, inyéctalo aquí para buscar el cliente
    // private final UsuarioService usuarioService;

    @Override
    @Transactional // Esto asegura que toda la operación sea atómica (o todo o nada)
    public Venta crearPedido(PedidoRequestDTO pedidoRequest) {

        // Busca todos los productos necesarios en una sola consulta a la BD
        List<Long> productoIds = pedidoRequest.getItems().stream().map(item -> item.getProductoId()).collect(Collectors.toList());
        Map<Long, Producto> productosMap = productoRepository.findAllById(productoIds).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        // Crea la entidad Venta principal
        Venta nuevaVenta = new Venta();
        nuevaVenta.setFechaVenta(LocalDate.now());
        nuevaVenta.setEstado("PROCESANDO");
        // Aquí buscarías y asignarías el usuario real
        // Usuario cliente = usuarioService.buscarPorId(pedidoRequest.getClienteId());
        // nuevaVenta.setCliente(cliente);

        Set<DetalleVenta> detalles = new HashSet<>();
        BigDecimal totalPedido = BigDecimal.ZERO;

        // Crea cada detalle de la venta
        for (var itemDTO : pedidoRequest.getItems()) {
            Producto producto = productosMap.get(itemDTO.getProductoId());
            if (producto == null) throw new RuntimeException("Producto no encontrado con ID: " + itemDTO.getProductoId());

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(itemDTO.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio().multiply(new BigDecimal(itemDTO.getCantidad())));
            detalle.setVenta(nuevaVenta); // Enlaza el detalle con la venta

            detalles.add(detalle);
            totalPedido = totalPedido.add(detalle.getSubtotal());

            // Lógica para actualizar el stock del producto
            // int nuevoStock = producto.getStock() - itemDTO.getCantidad();
            // if (nuevoStock < 0) throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            // producto.setStock(nuevoStock);
            // productoRepository.save(producto);
        }

        nuevaVenta.setDetallesVenta((List<DetalleVenta>) detalles);
        nuevaVenta.setTotal(totalPedido);

        // Guarda la Venta y sus Detalles en la base de datos
        return ventaRepository.save(nuevaVenta);
    }
}