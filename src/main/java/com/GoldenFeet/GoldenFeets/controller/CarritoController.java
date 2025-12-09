package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.entity.*;
import com.GoldenFeet.GoldenFeets.repository.*;
import com.GoldenFeet.GoldenFeets.service.EmailService;
import com.GoldenFeet.GoldenFeets.service.PdfService;
import com.GoldenFeet.GoldenFeets.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CarritoController {

    private final UsuarioService usuarioService;
    private final VentaRepository ventaRepository;
    // private final ProductoRepository productoRepository; // Ya no lo usamos directamente para el stock
    private final VarianteProductoRepository varianteRepository; // NUEVA DEPENDENCIA
    private final EntregaRepository entregaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PdfService pdfService;
    private final EmailService emailService;

    // === 1. VER CARRITO (Vista HTML) ===
    @GetMapping("/carrito")
    public String verCarrito(HttpSession session, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean usuarioAutenticado = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName());

        model.addAttribute("usuarioAutenticado", usuarioAutenticado);

        if (usuarioAutenticado) {
            Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());
            if (usuario != null) model.addAttribute("usuario", usuario);
        }
        return "carrito";
    }

    // === 2. API PARA PROCESAR EL PEDIDO ===
    @PostMapping("/api/carrito/procesar")
    @ResponseBody
    @Transactional // CRÍTICO: Si falla algo, revierte el descuento de stock
    public ResponseEntity<Map<String, Object>> crearPedidoAPI(@RequestBody PedidoRequest request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            response.put("error", "Debes iniciar sesión.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            response.put("error", "El carrito está vacío.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 1. Actualizar datos del usuario con la info de envío
            Usuario usuario = usuarioService.buscarPorEmail(auth.getName());
            if (request.getDireccion() != null) usuario.setDireccion(request.getDireccion());
            if (request.getDepartamento() != null) usuario.setDepartamento(request.getDepartamento());
            if (request.getCiudad() != null) usuario.setCiudad(request.getCiudad());
            if (request.getLocalidad() != null) usuario.setLocalidad(request.getLocalidad());
            if (request.getBarrio() != null) usuario.setBarrio(request.getBarrio());
            if (request.getInformacionAdicional() != null) usuario.setInformacionAdicional(request.getInformacionAdicional());
            usuarioRepository.save(usuario);

            // 2. Crear Venta Cabecera
            Venta venta = new Venta();
            venta.setCliente(usuario);
            venta.setFechaVenta(LocalDate.now());
            venta.setEstado("Pendiente");
            venta.setDireccionEnvio(request.getDireccion());
            venta.setCiudadEnvio(request.getCiudad());
            venta.setLocalidad(request.getLocalidad());

            if (venta.getDetallesVenta() == null) {
                venta.setDetallesVenta(new ArrayList<>());
            }

            venta = ventaRepository.save(venta);

            BigDecimal totalVenta = BigDecimal.ZERO;
            List<DetalleVenta> detallesParaGuardar = new ArrayList<>();

            // 3. Procesar Items y DESCONTAR STOCK (USANDO VARIANTES)
            for (ItemPedido itemRequest : request.getItems()) {

                // Conversión de datos del request
                Long productoId = Long.valueOf(itemRequest.getProductoId());
                Integer tallaRequerida = Integer.parseInt(itemRequest.getTalla());
                String colorRequerido = itemRequest.getColor();

                // BUSCAR LA VARIANTE ESPECÍFICA
                // CORRECCIÓN: Usamos el método findByProductoId
                List<VarianteProducto> variantesDelProducto = varianteRepository.findByProductoId(productoId);

                VarianteProducto variante = variantesDelProducto.stream()
                        .filter(v -> v.getTalla().equals(tallaRequerida) && v.getColor().equalsIgnoreCase(colorRequerido))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Variante no encontrada: " + colorRequerido + " Talla " + tallaRequerida));

                // --- A. VALIDAR Y DESCONTAR STOCK EN LA VARIANTE ---
                if (variante.getStock() < itemRequest.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + variante.getSku() + ". Disponible: " + variante.getStock());
                }

                // Actualizar stock
                variante.setStock(variante.getStock() - itemRequest.getCantidad());
                varianteRepository.save(variante);

                // --- B. CREAR DETALLE ---
                DetalleVenta detalle = new DetalleVenta();
                detalle.setVenta(venta);

                // IMPORTANTE: Ahora asignamos la variante, no el producto directo
                detalle.setVariante(variante);

                detalle.setCantidad(itemRequest.getCantidad());

                // Guardamos talla y color como histórico (String)
                detalle.setTalla(String.valueOf(variante.getTalla()));
                detalle.setColor(variante.getColor());

                // Precio viene del Producto Padre
                BigDecimal precioUnitario = BigDecimal.valueOf(variante.getProducto().getPrecio());
                detalle.setPrecioUnitario(precioUnitario);

                BigDecimal subtotalDetalle = precioUnitario.multiply(BigDecimal.valueOf(itemRequest.getCantidad()));
                detalle.setSubtotal(subtotalDetalle);

                detallesParaGuardar.add(detalle);
                totalVenta = totalVenta.add(subtotalDetalle);
            }

            venta.setTotal(totalVenta);
            venta.getDetallesVenta().clear();
            venta.getDetallesVenta().addAll(detallesParaGuardar);
            ventaRepository.save(venta);

            // 4. Crear Entrega
            Entrega entrega = new Entrega();
            entrega.setVenta(venta);
            entrega.setEstado("Pendiente");
            entrega.setFechaCreacion(LocalDateTime.now());
            entrega.setLocalidad(request.getLocalidad());
            entregaRepository.save(entrega);

            // 5. PDF y Correo
            try {
                // NOTA: Asegúrate que pdfService sepa leer detalle.getVariante().getProducto().getNombre()
                ByteArrayInputStream pdfFactura = pdfService.generarFacturaVenta(venta);
                emailService.enviarConfirmacionCompra(venta, pdfFactura);
            } catch (Exception e) {
                System.err.println("Advertencia: No se pudo enviar el correo: " + e.getMessage());
            }

            session.removeAttribute("carrito");

            response.put("success", true);
            response.put("idVenta", venta.getIdVenta());
            response.put("clienteEmail", usuario.getEmail());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error procesando pedido: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // DTOs Internos
    @Data
    static class PedidoRequest {
        private List<ItemPedido> items;
        private String direccion;
        private String departamento;
        private String ciudad;
        private String localidad;
        private String barrio;
        private String informacionAdicional;
        private String metodoPago;
    }

    @Data
    static class ItemPedido {
        private Integer productoId;
        private Integer cantidad;
        private String talla;
        private String color;
    }
}