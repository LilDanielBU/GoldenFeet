package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.entity.Venta;
import com.GoldenFeet.GoldenFeets.entity.VarianteProducto; // NUEVO IMPORT
import com.GoldenFeet.GoldenFeets.repository.VarianteProductoRepository; // NUEVO IMPORT
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import com.GoldenFeet.GoldenFeets.service.UsuarioService;
import com.GoldenFeet.GoldenFeets.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired private VentaService ventaService;
    @Autowired private ProductoService productoService;
    @Autowired private UsuarioService usuarioService;
    // --- NUEVA INYECCIÓN ---
    @Autowired private VarianteProductoRepository varianteRepository;

    // 1. ELIMINAR USUARIO
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuarioApi(@PathVariable Integer id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    // 2. CARGAR USUARIOS
    @GetMapping("/usuarios")
    public ResponseEntity<?> obtenerTodosLosUsuarios() {
        try {
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            // Filtramos y convertimos
            List<Map<String, Object>> result = usuarios.stream()
                    .filter(u -> u.getRoles().stream().anyMatch(r -> !r.getNombre().equals("ROLE_CLIENTE")))
                    .map(u -> Map.of(
                            "id", u.getIdUsuario(),
                            "nombre", u.getNombre() != null ? u.getNombre() : "",
                            "email", u.getEmail() != null ? u.getEmail() : "",
                            "localidad", u.getLocalidad() != null ? u.getLocalidad() : "",
                            "activo", u.isActivo(),
                            "roles", u.getRoles().stream().map(r -> r.getNombre()).collect(Collectors.toList())
                    )).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 3. ESTADÍSTICAS GENERALES Y ACTIVIDAD (CORREGIDO PARA VARIANTES)
    @GetMapping("/stats")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            // --- A. INVENTARIO (CORREGIDO) ---
            Collection<Producto> productos = productoService.listarProductos(); // Total de modelos (productos padre)
            if (productos == null) productos = new ArrayList<>();
            long totalProductos = productos.size();

            // 1. Obtenemos todas las variantes para el cálculo de stock real
            List<VarianteProducto> variantes = varianteRepository.findAll();

            // 2. Calculamos KPIs basados en las variantes
            long stockBajo = variantes.stream().filter(v -> v.getStock() > 0 && v.getStock() < 5).count();
            long sinStock = variantes.stream().filter(v -> v.getStock() == 0).count();

            double valorInventario = 0;
            try { valorInventario = productoService.calcularValorTotalInventario(); } catch (Exception e) {}

            // --- B. USUARIOS ---
            long usuariosActivos = usuarioService.contarUsuariosActivos();

            // --- C. VENTAS Y COMPRAS PENDIENTES ---
            long comprasPendientes = 0;
            List<Venta> todasLasVentas = new ArrayList<>();
            try {
                Collection<Venta> v = ventaService.obtenerTodasLasVentas();
                if (v != null) {
                    todasLasVentas.addAll(v);
                    comprasPendientes = todasLasVentas.stream()
                            .filter(x -> x.getEstado() != null && x.getEstado().toUpperCase().contains("PENDIENTE"))
                            .count();
                }
            } catch (Exception e) {}

            // --- D. CONSTRUIR ACTIVIDAD RECIENTE ---
            List<Map<String, Object>> actividad = new ArrayList<>();

            // 1. Últimas 2 Ventas
            todasLasVentas.sort((v1, v2) -> { // Ordenar por ID descendente (más reciente)
                if(v1.getIdVenta() == null) return 1;
                if(v2.getIdVenta() == null) return -1;
                return v2.getIdVenta().compareTo(v1.getIdVenta());
            });

            for (int i = 0; i < Math.min(2, todasLasVentas.size()); i++) {
                Venta v = todasLasVentas.get(i);
                Map<String, Object> item = new HashMap<>();
                item.put("tipo", "venta");
                item.put("titulo", "Nueva Venta #" + v.getIdVenta());
                item.put("desc", "Total: $" + v.getTotal());
                item.put("tiempo", "Reciente");
                actividad.add(item);
            }

            // 2. Últimos 2 Productos Agregados (Modelos)
            List<Producto> listaProd = new ArrayList<>(productos);
            // Si no hay campo de fecha de creación, se asume el orden inverso de la lista (último agregado)
            Collections.reverse(listaProd);

            for (int i = 0; i < Math.min(2, listaProd.size()); i++) {
                Producto p = listaProd.get(i);
                Map<String, Object> item = new HashMap<>();
                item.put("tipo", "producto");
                item.put("titulo", "Producto Agregado");
                // FIX: Ya no hay stock en el producto padre, solo mostramos el modelo
                item.put("desc", p.getNombre() + " (Modelo)");
                item.put("tiempo", "Reciente");
                actividad.add(item);
            }

            // --- E. RESPUESTA FINAL ---
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProductos", totalProductos); // Total de modelos
            stats.put("stockBajo", stockBajo); // Basado en variantes
            stats.put("sinStock", sinStock);   // Basado en variantes
            stats.put("comprasPendientes", comprasPendientes);
            stats.put("usuariosActivos", usuariosActivos);
            stats.put("valorInventario", valorInventario);
            stats.put("actividadReciente", actividad);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // 4. REPORTES VENTAS
    @GetMapping("/ventas/reportes")
    public ResponseEntity<?> obtenerReportesVentas() {
        try {
            double ventaTotal = ventaService.obtenerVentasDelMes();
            int unidades = ventaService.obtenerUnidadesVendidasMes();
            double ticket = ventaService.obtenerTicketPromedioMes();
            Map<String, Double> historia = ventaService.obtenerVentasUltimosMeses();

            return ResponseEntity.ok(Map.of(
                    "ventaTotalMes", ventaTotal,
                    "unidadesVendidasMes", unidades,
                    "ticketPromedio", ticket,
                    "meses", new ArrayList<>(historia.keySet()),
                    "ventasMensuales", new ArrayList<>(historia.values())
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- 5. HISTORIAL DE VENTAS (TABLA) ---
    @GetMapping("/ventas/historial")
    public ResponseEntity<?> obtenerHistorialVentas() {
        try {
            List<Venta> listaVentas = new ArrayList<>(ventaService.obtenerTodasLasVentas());

            if (listaVentas.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            listaVentas.sort((v1, v2) -> {
                if (v1.getIdVenta() == null) return 1;
                if (v2.getIdVenta() == null) return -1;
                return v2.getIdVenta().compareTo(v1.getIdVenta());
            });

            List<Map<String, Object>> historial = new ArrayList<>();

            for (int i = 0; i < Math.min(10, listaVentas.size()); i++) {
                Venta v = listaVentas.get(i);

                String clienteNombre = "Cliente Desconocido";
                try {
                    if (v.getCliente() != null) {
                        String nombre = v.getCliente().getNombre() != null ? v.getCliente().getNombre() : "";
                        String apellido = v.getCliente().getApellido() != null ? v.getCliente().getApellido() : "";
                        clienteNombre = (nombre + " " + apellido).trim();
                        if (clienteNombre.isEmpty()) clienteNombre = v.getCliente().getEmail();
                    }
                } catch (Exception e) { System.out.println("Error leyendo cliente: " + e.getMessage()); }

                String fechaStr = "Fecha desconocida";
                if (v.getFechaVenta() != null) {
                    fechaStr = v.getFechaVenta().toString();
                }

                Map<String, Object> map = new HashMap<>();
                map.put("id", v.getIdVenta());
                map.put("cliente", clienteNombre);
                map.put("total", v.getTotal() != null ? v.getTotal() : 0);
                map.put("estado", v.getEstado() != null ? v.getEstado() : "Pendiente");
                map.put("fecha", fechaStr);

                historial.add(map);
            }

            return ResponseEntity.ok(historial);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cargar historial: " + e.getMessage());
        }
    }
}