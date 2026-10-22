package pe.edu.tecsup.examen1.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.edu.tecsup.examen1.dto.CitaDTO;
import pe.edu.tecsup.examen1.entity.EstadoCita;
import pe.edu.tecsup.examen1.entity.Paciente;
import pe.edu.tecsup.examen1.service.CitaService;
import pe.edu.tecsup.examen1.service.PacienteService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/citas")
public class CitaViewController {

    private static final String FORMULARIO = "citas/formulario";
    private static final String REDIRECT_CITAS = "redirect:/citas";

    private final CitaService citaService;
    private final PacienteService pacienteService;

    public CitaViewController(CitaService citaService, PacienteService pacienteService) {
        this.citaService = citaService;
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("citas", citaService.listar());
        return "citas/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("citaForm", new CitaDTO());
        return formulario(model, null);
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        CitaDTO dto = citaService.aDTO(citaService.buscarPorId(id));
        model.addAttribute("citaForm", dto);
        return formulario(model, dto.getPacienteId());
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("citaForm") CitaDTO dto,
                          BindingResult result, Model model) {
        if (result.hasErrors()) {
            return formulario(model, dto.getPacienteId());
        }
        try {
            if (dto.getId() == null) {
                citaService.registrar(dto);
            } else {
                citaService.modificar(dto.getId(), dto);
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, dto.getPacienteId());
        }
        return REDIRECT_CITAS;
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        citaService.eliminar(id);
        return REDIRECT_CITAS;
    }

    // Pacientes activos; al editar se agrega el de la cita aunque esté inactivo
    private String formulario(Model model, Long pacienteActualId) {
        List<Paciente> pacientes = new ArrayList<>(pacienteService.listarActivos());
        if (pacienteActualId != null && pacientes.stream().noneMatch(p -> p.getId().equals(pacienteActualId))) {
            pacientes.add(0, pacienteService.buscarPorId(pacienteActualId));
        }
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("medicos", citaService.listarMedicos());
        model.addAttribute("estados", EstadoCita.values());
        return FORMULARIO;
    }
}
