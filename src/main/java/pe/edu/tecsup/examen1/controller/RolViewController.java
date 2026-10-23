package pe.edu.tecsup.examen1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.tecsup.examen1.entity.Rol;
import pe.edu.tecsup.examen1.service.RolService;

import java.util.Set;

@Controller
@RequestMapping("/roles")
public class RolViewController {

    private final RolService rolService;

    public RolViewController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public String listarRoles(Model model) {
        model.addAttribute("roles", rolService.listarTodos());
        return "roles/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoRol(Model model) {
        model.addAttribute("rolForm", new Rol());
        return "roles/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editarRol(@PathVariable Long id, Model model) {
        Rol rol = rolService.obtenerPorId(id);
        model.addAttribute("rolForm", rol);
        return "roles/formulario";
    }

    @PostMapping("/guardar")
    public String guardarRol(@ModelAttribute("rolForm") Rol rol) {
        rolService.guardarRol(rol);
        return "redirect:/roles";
    }

    @GetMapping("/desactivar/{id}")
    public String desactivarRol(@PathVariable Long id) {
        rolService.cambiarEstado(id, false);
        return "redirect:/roles";
    }

    @GetMapping("/activar/{id}")
    public String activarRol(@PathVariable Long id) {
        rolService.cambiarEstado(id, true);
        return "redirect:/roles";
    }

    @GetMapping("/permisos/{id}")
    public String asignarPermisos(@PathVariable Long id, Model model) {
        Rol rol = rolService.obtenerPorId(id);

        model.addAttribute("rol", rol);
        model.addAttribute("permisos", rolService.listarPermisos());

        return "roles/permisos";
    }

    @PostMapping("/permisos/guardar")
    public String guardarPermisos(
            @RequestParam Long rolId,
            @RequestParam(required = false) Set<Long> permisosIds) {

        rolService.asignarPermisos(
                rolId,
                permisosIds != null ? permisosIds : Set.of()
        );

        return "redirect:/roles";
    }
}