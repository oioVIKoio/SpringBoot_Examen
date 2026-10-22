package pe.edu.tecsup.examen1.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.edu.tecsup.examen1.dto.HistoriaClinicaDTO;
import pe.edu.tecsup.examen1.service.HistoriaClinicaService;
import pe.edu.tecsup.examen1.service.PacienteService;

@Controller
@RequestMapping("/historias")
public class HistoriaClinicaViewController {

    private static final String FORMULARIO = "historias/formulario";

    private final HistoriaClinicaService historiaService;
    private final PacienteService pacienteService;

    public HistoriaClinicaViewController(HistoriaClinicaService historiaService,
                                         PacienteService pacienteService) {
        this.historiaService = historiaService;
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("historias", historiaService.listar());
        return "historias/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("historiaForm", new HistoriaClinicaDTO());
        return formulario(model, null);
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        HistoriaClinicaDTO dto = historiaService.aDTO(historiaService.buscarPorId(id));
        model.addAttribute("historiaForm", dto);
        return formulario(model, dto.getPacienteId());
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("historiaForm") HistoriaClinicaDTO dto,
                          BindingResult result, Model model) {
        Long pacienteId = dto.getId() != null ? historiaService.buscarPorId(dto.getId()).getPaciente().getId() : null;
        if (result.hasErrors()) {
            return formulario(model, pacienteId);
        }
        try {
            if (dto.getId() == null) {
                historiaService.registrar(dto);
            } else {
                historiaService.modificar(dto.getId(), dto);
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, pacienteId);
        }
        return "redirect:/historias";
    }

    // Nueva: pacientes sin historia · Editar: el paciente de la historia (no cambia)
    private String formulario(Model model, Long pacienteId) {
        if (pacienteId == null) {
            model.addAttribute("pacientes", pacienteService.listarSinHistoria());
        } else {
            model.addAttribute("paciente", pacienteService.buscarPorId(pacienteId));
        }
        return FORMULARIO;
    }
}
