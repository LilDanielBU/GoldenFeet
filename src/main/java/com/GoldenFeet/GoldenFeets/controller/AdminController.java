package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.dto.AdminUsuarioUpdateDTO;
import com.GoldenFeet.GoldenFeets.dto.ProductoDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioFormDTO;
import com.GoldenFeet.GoldenFeets.dto.UsuarioRegistroDTO;
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
    @GetMapping("/usuarios/crear") // <--- NUEVA RUTA PARA MOSTRAR EL FORMULARIO
    public String mostrarFormularioCrear(Model model) {
        // Objeto DTO vacío para vincular al formulario
        model.addAttribute("nuevoUsuario", new UsuarioFormDTO());

        // Lista de roles y localidades necesarias para los <select>
        List<Rol> rolesTodos = rolService.listarTodosLosRoles();
        model.addAttribute("rolesTodos", rolesTodos);
        model.addAttribute("localidades", localidadesBogota); // Lista de localidades que ya tenías

        // Retorna el nombre de la nueva plantilla
        return "admin-usuario-new";
    }

    @GetMapping("/panel")
    public String mostrarPanel(Model model) {
        // Estadísticas básicas
        long totalUsuarios = usuarioService.contarUsuarios();
        long usuariosActivos = usuarioService.contarUsuariosActivos();
        long usuariosInactivos = usuarioService.contarUsuariosInactivos();

        long totalVentas = ventaService.contarVentas();
        double totalIngresos = ventaService.obtenerTotalIngresos();

        // 🔹 Traemos productos reales (no DTOs)
        Collection<Producto> productos = productoService.listarProductos();

        // 🔹 Calculamos valor total de inventario
        double valorInventario = productoService.calcularValorTotalInventario();

        // 🔹 Agregamos todo al modelo
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("usuariosActivos", usuariosActivos);
        model.addAttribute("usuariosInactivos", usuariosInactivos);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("productos", productos);
        model.addAttribute("valorInventario", valorInventario);

        return "admin-panel";
    }


    @GetMapping("/usuarios")
    public String mostrarUsuarios(Model model) {

        List<Usuario> todosLosUsuarios = usuarioService.obtenerTodosLosUsuarios();


        List<Usuario> usuariosAdministrativos = todosLosUsuarios.stream()
                .filter(usuario ->
                        usuario.getRoles().stream()
                                .anyMatch(rol -> !rol.getNombre().equals("ROLE_CLIENTE"))
                )
                .collect(Collectors.toList());

        model.addAttribute("usuarios", usuariosAdministrativos);

        model.addAttribute("nuevoUsuario", new UsuarioFormDTO());
        List<Rol> rolesTodos = rolService.listarTodosLosRoles();
        model.addAttribute("rolesTodos", rolesTodos);
        model.addAttribute("localidades", localidadesBogota);

        return "admin-panel";
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

    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuario(AdminUsuarioUpdateDTO usuarioDto, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.actualizarUsuarioAdmin(usuarioDto);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarNuevoUsuario(@ModelAttribute("nuevoUsuario") UsuarioFormDTO usuarioDto, RedirectAttributes redirectAttributes) {
        try {
            Set<Integer> rolesIdSet = (usuarioDto.getRolesId() != null) ? new HashSet<>(usuarioDto.getRolesId()) : Set.of();

            UsuarioRegistroDTO registroDTO = new UsuarioRegistroDTO(
                    usuarioDto.getNombre(),
                    usuarioDto.getEmail(),
                    usuarioDto.getDireccion(),
                    usuarioDto.getLocalidad(),
                    usuarioDto.getFecha_nacimiento(),
                    usuarioDto.getTipo_documento(),
                    usuarioDto.getNumero_documento(),
                    usuarioDto.getTelefono(),
                    usuarioDto.getPassword(),
                    rolesIdSet
            );


            usuarioService.guardarUsuario(registroDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario creado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el usuario: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin/usuarios";
    }





    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario. Es posible que esté asociado a ventas u otros registros.");
        }
        return "redirect:/admin/usuarios";
    }
}