package pe.edu.tecsup.examen1.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.tecsup.examen1.dto.PacienteDTO;
import pe.edu.tecsup.examen1.service.PacienteService;

@Controller
@RequestMapping("/pacientes")
public class PacienteViewController {

    private static final String FORMULARIO = "pacientes/formulario";
    private static final String REDIRECT_PACIENTES = "redirect:/pacientes";

    private final PacienteService pacienteService;

    public PacienteViewController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pacientes", pacienteService.listar());
        return "pacientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("pacienteForm", new PacienteDTO());
        return FORMULARIO;
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("pacienteForm", pacienteService.aDTO(pacienteService.buscarPorId(id)));
        return FORMULARIO;
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("pacienteForm") PacienteDTO dto,
                          BindingResult result, Model model) {
        if (result.hasErrors()) {
            return FORMULARIO;
        }
        try {
            if (dto.getId() == null) {
                pacienteService.registrar(dto);
            } else {
                pacienteService.modificar(dto.getId(), dto);
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return FORMULARIO;
        }
        return REDIRECT_PACIENTES;
    }

    @GetMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id) {
        pacienteService.cambiarEstado(id, false);
        return REDIRECT_PACIENTES;
    }

    @GetMapping("/activar/{id}")
    public String activar(@PathVariable Long id) {
        pacienteService.cambiarEstado(id, true);
        return REDIRECT_PACIENTES;
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            pacienteService.eliminar(id);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECT_PACIENTES;
    }
}
