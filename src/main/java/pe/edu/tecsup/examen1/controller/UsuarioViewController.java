package pe.edu.tecsup.examen1.controller;

import pe.edu.tecsup.examen1.entity.Usuario;
import pe.edu.tecsup.examen1.service.UsuarioService;
import pe.edu.tecsup.examen1.service.RolService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
@RequestMapping("/usuarios")
public class UsuarioViewController {

    private final RolService rolService;




    private static final String REDIRECT_USUARIOS = "redirect:/usuarios";
    private final UsuarioService usuarioService;

    public UsuarioViewController(
            UsuarioService usuarioService,
            RolService rolService) {

        this.usuarioService = usuarioService;
        this.rolService = rolService;
    }
    @GetMapping("/roles/{id}")
    public String asignarRoles(
            @PathVariable Long id,
            Model model) {

        Usuario usuario = usuarioService.buscarPorId(id);

        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", rolService.listarTodos());

        return "usuarios/roles";
    }
    @PostMapping("/roles/guardar")
    public String guardarRoles(
            @RequestParam Long usuarioId,
            @RequestParam(required = false) Set<Long> rolesIds) {

        usuarioService.asignarRoles(
                usuarioId,
                rolesIds != null ? rolesIds : Set.of()
        );

        return REDIRECT_USUARIOS;
    }
    @GetMapping
    public String listarUsuarios(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) Long rolId,
            @RequestParam(required = false) Boolean activo,
            Model model) {

        model.addAttribute(
                "usuarios",
                usuarioService.buscarUsuarios(
                        termino,
                        rolId,
                        activo
                )
        );

        model.addAttribute(
                "roles",
                rolService.listarTodos()
        );

        model.addAttribute("termino", termino);
        model.addAttribute("rolId", rolId);
        model.addAttribute("activo", activo);

        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoUsuario(Model model) {
        model.addAttribute("usuarioForm", new Usuario());
        return "usuarios/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {

        Usuario usuario = usuarioService.buscarPorId(id);

        model.addAttribute("usuarioForm", usuario);

        return "usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(
            @ModelAttribute("usuarioForm") Usuario usuario,
            Model model) {

        try {

            if (usuario.getId() == null) {
                usuarioService.registrarUsuario(usuario);
            } else {
                usuarioService.modificarUsuario(usuario.getId(), usuario);
            }

            return REDIRECT_USUARIOS;

        } catch (IllegalArgumentException e) {

            model.addAttribute("errorPassword", e.getMessage());

            return "usuarios/formulario";
        }
    }
    @GetMapping("/desactivar/{id}")
    public String desactivarUsuario(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            usuarioService.cambiarEstado(id, false);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECT_USUARIOS;
    }

    @GetMapping("/activar/{id}")
    public String activarUsuario(@PathVariable Long id) {
        usuarioService.cambiarEstado(id, true);
        return REDIRECT_USUARIOS;
    }
}