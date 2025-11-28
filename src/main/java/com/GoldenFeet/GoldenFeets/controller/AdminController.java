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
        long totalUsuarios = usuarioService.contarUsuarios();
        long usuariosActivos = usuarioService.contarUsuariosActivos();
        long usuariosInactivos = usuarioService.contarUsuariosInactivos();

        long totalVentas = ventaService.contarVentas();
        double totalIngresos = ventaService.obtenerTotalIngresos();

        Collection<Producto> productos = productoService.listarProductos();
        double valorInventario = productoService.calcularValorTotalInventario();

        List<VentaResponseDTO> ultimasVentas = ventaService.obtenerTodasLasVentas()
                .stream()
                .limit(5)
                .map(VentaResponseDTO::fromEntity)
                .collect(Collectors.toList());

        if (ultimasVentas == null) {
            ultimasVentas = List.of();
        }

        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("usuariosActivos", usuariosActivos);
        model.addAttribute("usuariosInactivos", usuariosInactivos);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("productos", productos);
        model.addAttribute("valorInventario", valorInventario);
        model.addAttribute("ultimasVentas", ultimasVentas);

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
        model.addAttribute("rolesTodos", rolService.listarTodosLosRoles());
        model.addAttribute("localidades", localidadesBogota);

        return "admin-usuarios";
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
            Set<Integer> rolesIdSet = (usuarioDto.getRolesId() != null)
                    ? new HashSet<>(usuarioDto.getRolesId())
                    : Set.of();

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
        }

        return "admin-panel";
    }

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario. Es posible que esté asociado a ventas u otros registros.");
        }
        return "redirect:panel";
    }
    @GetMapping("/compras")
    public String verComprasAdmin(Model model) {


        List<VentaResponseDTO> compras = ventaService.obtenerTodasLasVentas()
                .stream()
                .map(VentaResponseDTO::fromEntity)
                .collect(Collectors.toList());

        model.addAttribute("compras", compras);

        // 📊 Estadísticas
        double totalVentasMes = ventaService.obtenerVentasDelMes();
        int unidadesVendidasMes = ventaService.obtenerUnidadesVendidasMes();
        double ticketPromedio = ventaService.obtenerTicketPromedioMes();

        model.addAttribute("monthlySalesValue", totalVentasMes);
        model.addAttribute("monthlyUnitsValue", unidadesVendidasMes);
        model.addAttribute("avgTicketValue", ticketPromedio);

        // 📈 Datos para la gráfica de los últimos 6 meses
        Map<String, Double> ventasMensuales = ventaService.obtenerVentasUltimosMeses();
        model.addAttribute("ventasMensuales", ventasMensuales);

        return "admin-panel";
    }

}
