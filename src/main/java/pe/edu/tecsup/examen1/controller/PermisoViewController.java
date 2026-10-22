package pe.edu.tecsup.examen1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.tecsup.examen1.entity.Permiso;
import pe.edu.tecsup.examen1.service.PermisoService;

@Controller
@RequestMapping("/permisos")
public class PermisoViewController {

    private final PermisoService permisoService;

    public PermisoViewController(
            PermisoService permisoService) {

        this.permisoService = permisoService;
    }

    @GetMapping
    public String listarPermisos(Model model) {

        model.addAttribute(
                "permisos",
                permisoService.listar()
        );

        return "permisos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoPermiso(Model model) {

        model.addAttribute(
                "permisoForm",
                new Permiso()
        );

        return "permisos/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editarPermiso(
            @PathVariable Long id,
            Model model) {

        Permiso permiso =
                permisoService.buscarPorId(id);

        model.addAttribute(
                "permisoForm",
                permiso
        );

        return "permisos/formulario";
    }

    @PostMapping("/guardar")
    public String guardarPermiso(
            @ModelAttribute("permisoForm") Permiso permiso) {

        permisoService.guardar(permiso);

        return "redirect:/permisos";
    }
}