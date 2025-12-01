package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.AdminUsuarioUpdateDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioFormDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioRegistroDTO;
import com.GoldenFeet.GoldenFeets.dto.VentaResponseDTO;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.entity.Rol;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import com.GoldenFeet.GoldenFeets.service.ProductoService;
import com.GoldenFeet.GoldenFeets.service.RolService;
import com.GoldenFeet.GoldenFeets.service.UsuarioService;
import com.GoldenFeet.GoldenFeets.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    @Autowired
    private ProductoService productoService;

    private final List<String> localidadesBogota = Arrays.asList(
            "Usaquén", "Chapinero", "Santa Fe", "San Cristóbal", "Usme", "Tunjuelito",
            "Bosa", "Kennedy", "Fontibón", "Engativá", "Suba", "Barrios Unidos",
            "Teusaquillo", "Los Mártires", "Antonio Nariño", "Puente Aranda",
            "La Candelaria", "Rafael Uribe Uribe", "Ciudad Bolívar", "Sumapaz"
    );

    @GetMapping("/usuarios/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("nuevoUsuario", new UsuarioFormDTO());
        model.addAttribute("rolesTodos", rolService.listarTodosLosRoles());
        model.addAttribute("localidades", localidadesBogota);
        return "admin-usuario-new";
    }

    @GetMapping("/panel")
    public String mostrarPanel(Model model) {
        // Cargar datos iniciales básicos (El resto lo hace el JS con la API)
        long totalUsuarios = usuarioService.contarUsuarios();
        long usuariosActivos = usuarioService.contarUsuariosActivos();
        long usuariosInactivos = usuarioService.contarUsuariosInactivos();
        long totalVentas = 0;
        try { totalVentas = ventaService.contarVentas(); } catch(Exception e){}

        double totalIngresos = 0;
        try { totalIngresos = ventaService.obtenerTotalIngresos(); } catch(Exception e){}

        Collection<Producto> productos = productoService.listarProductos();
        double valorInventario = 0;
        try { valorInventario = productoService.calcularValorTotalInventario(); } catch(Exception e){}

        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("usuariosActivos", usuariosActivos);
        model.addAttribute("usuariosInactivos", usuariosInactivos);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("productos", productos); // Importante para la tabla inicial
        model.addAttribute("valorInventario", valorInventario);

        return "admin-panel";
    }

    @GetMapping("/usuarios")
    public String mostrarUsuarios(Model model) {
        // Este método quizás ya no se use si todo es AJAX, pero lo dejamos por si acaso
        List<Usuario> todosLosUsuarios = usuarioService.obtenerTodosLosUsuarios();
        List<Usuario> usuariosAdministrativos = todosLosUsuarios.stream()
                .filter(usuario -> usuario.getRoles().stream()
                        .anyMatch(rol -> !rol.getNombre().equals("ROLE_CLIENTE")))
                .collect(Collectors.toList());

        model.addAttribute("usuarios", usuariosAdministrativos);
        return "admin-usuarios"; // Asegúrate que esta vista exista si la usas
    }

    @GetMapping("/usuarios/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
        List<Rol> rolesTodos = rolService.listarTodosLosRoles();

        AdminUsuarioUpdateDTO usuarioDto = new AdminUsuarioUpdateDTO();
        usuarioDto.setIdUsuario(usuario.getIdUsuario());
        usuarioDto.setNombre(usuario.getNombre());
        usuarioDto.setEmail(usuario.getEmail());
        usuarioDto.setActivo(usuario.isActivo());
        usuarioDto.setLocalidad(usuario.getLocalidad());

        Set<Integer> rolesIds = usuario.getRoles().stream()
                .map(Rol::getIdRol)
                .collect(Collectors.toSet());

        usuarioDto.setRolesId(rolesIds);

        model.addAttribute("usuario", usuarioDto);
        model.addAttribute("rolesTodos", rolesTodos);
        model.addAttribute("localidades", localidadesBogota);

        return "admin-usuario-edit";
    }

    // --- AQUÍ ESTÁN LAS CORRECCIONES CLAVE ---

    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuario(AdminUsuarioUpdateDTO usuarioDto, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.actualizarUsuarioAdmin(usuarioDto);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar: " + e.getMessage());
        }
        // REDIRECT AL PANEL CON LA PESTAÑA DE USUARIOS ACTIVA
        return "redirect:/admin/panel?tab=usuarios";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarNuevoUsuario(@ModelAttribute("nuevoUsuario") UsuarioFormDTO usuarioDto, RedirectAttributes redirectAttributes) {
        try {
            Set<Integer> rolesIdSet = (usuarioDto.getRolesId() != null) ? new HashSet<>(usuarioDto.getRolesId()) : Set.of();

            UsuarioRegistroDTO registroDTO = new UsuarioRegistroDTO(
                    usuarioDto.getNombre(), usuarioDto.getEmail(), usuarioDto.getDireccion(),
                    usuarioDto.getLocalidad(), usuarioDto.getFecha_nacimiento(),
                    usuarioDto.getTipo_documento(), usuarioDto.getNumero_documento(),
                    usuarioDto.getTelefono(), usuarioDto.getPassword(), rolesIdSet
            );

            usuarioService.guardarUsuario(registroDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario creado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear: " + e.getMessage());
        }
        // REDIRECT AL PANEL CON LA PESTAÑA DE USUARIOS ACTIVA
        return "redirect:/admin/panel?tab=usuarios";
    }

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar.");
        }
        // REDIRECT AL PANEL CON LA PESTAÑA DE USUARIOS ACTIVA
        return "redirect:/admin/panel?tab=usuarios";
    }

    // Este método ya no es necesario si usas la API AJAX, pero lo dejamos por compatibilidad
    @GetMapping("/compras")
    public String verComprasAdmin(Model model) {
        return "redirect:/admin/panel?tab=compras";
    }
}