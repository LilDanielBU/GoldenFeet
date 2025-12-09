package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping; // Soluciona el error de la imagen si decides usarlo
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
// @RequestMapping("/public") // Descomenta esto si tuvieras una ruta base, si no, déjalo así.
public class CatalogoController {

    private final ProductoService productoService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/catalogo")
    public String verCatalogo(
            Model model,
            Principal principal, // Inyección del usuario autenticado (Spring Security)
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) List<String> marcas,
            @RequestParam(required = false) String busqueda) {

        // =======================================================
        // 1. LÓGICA DE USUARIO (Para mostrar nombre/avatar en navbar)
        // =======================================================
        if (principal != null) {
            String email = principal.getName();
            Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
            if (usuario.isPresent()) {
                model.addAttribute("usuario", usuario.get());
                // Log opcional para verificar en consola
                System.out.println("✅ Usuario detectado en catálogo: " + usuario.get().getNombre());
            }
        }

        // =======================================================
        // 2. LÓGICA DE FILTRADO Y BÚSQUEDA
        // =======================================================
        List<ProductoDTO> productos;
        String titulo;

        // Prioridad: Búsqueda por nombre
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            productos = productoService.buscarPorNombre(busqueda);
            titulo = "Resultados para: '" + busqueda + "'";
        } else {
            // Filtros normales
            productos = productoService.filtrarProductos(categoria, precioMax, marcas);

            if (categoria != null) {
                titulo = "Categoría: " + categoria;
            } else if (marcas != null && !marcas.isEmpty()) {
                titulo = "Marcas seleccionadas";
            } else {
                titulo = "Nuestro Catálogo";
            }
        }

        // Evitar nulos
        if (productos == null) {
            productos = List.of();
        }

        // =======================================================
        // 3. ENVIAR DATOS A LA VISTA
        // =======================================================
        model.addAttribute("productos", productos);
        model.addAttribute("tituloCatalogo", titulo);

        // Datos para los filtros laterales (Sidebar)
        model.addAttribute("productosDestacados", productoService.listarDestacados());
        model.addAttribute("categorias", productoService.listarCategorias());
        model.addAttribute("marcasDisponibles", productoService.listarMarcasDistintas());

        return "catalogo"; // Retorna la vista catalogo.html
    }
}