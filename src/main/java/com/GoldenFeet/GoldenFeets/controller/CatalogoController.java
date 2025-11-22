package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.entity.Usuario; // <--- 1. IMPORTAR ENTIDAD
import com.GoldenFeet.GoldenFeets.repository.UsuarioRepository; // <--- 1. IMPORTAR REPOSITORIO
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal; // <--- 1. IMPORTAR PRINCIPAL
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CatalogoController {

    private final ProductoService productoService;
    private final UsuarioRepository usuarioRepository; // <--- 2. INYECTAR REPOSITORIO

    @GetMapping("/catalogo")
    public String verCatalogo(
            Model model,
            Principal principal, // <--- 3. AGREGAR PRINCIPAL EN LOS PARÁMETROS
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) List<String> marcas,
            @RequestParam(required = false) String busqueda) {

        // === INICIO BLOQUE NUEVO: DETECTAR USUARIO ===
        if (principal != null) {
            String email = principal.getName();
            // Buscamos al usuario por email para tener su nombre e ID
            Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
            if (usuario.isPresent()) {
                model.addAttribute("usuario", usuario.get()); // Enviamos 'usuario' al HTML
                System.out.println("👤 Usuario logueado en catálogo: " + usuario.get().getNombre());
            }
        }
        // === FIN BLOQUE NUEVO ===

        List<ProductoDTO> productos;
        String titulo;

        // La búsqueda por nombre tiene la máxima prioridad
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            productos = productoService.buscarPorNombre(busqueda);
            titulo = "Resultados para: '" + busqueda + "'";
        } else {
            productos = productoService.filtrarProductos(categoria, precioMax, marcas);

            if (categoria != null) {
                titulo = "Categoría: " + categoria;
            } else if (marcas != null && !marcas.isEmpty()) {
                titulo = "Marcas seleccionadas";
            } else {
                titulo = "Nuestro Catálogo";
            }
        }

        if (productos == null) {
            productos = List.of();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("tituloCatalogo", titulo);
        model.addAttribute("productosDestacados", productoService.listarDestacados());
        model.addAttribute("categorias", productoService.listarCategorias());
        model.addAttribute("marcasDisponibles", productoService.listarMarcasDistintas());

        return "catalogo";
    }
}