package pe.edu.tecsup.examen1.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.tecsup.examen1.dto.PacienteDTO;
import pe.edu.tecsup.examen1.entity.Paciente;
import pe.edu.tecsup.examen1.service.PacienteService;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteRestController {

    private final PacienteService pacienteService;

    public PacienteRestController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public List<Paciente> listar() {
        return pacienteService.listar();
    }

    @GetMapping("/{id}")
    public Paciente obtener(@PathVariable Long id) {
        return pacienteService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody PacienteDTO dto) {
        return ResponseEntity.status(201).body(pacienteService.registrar(dto));
    }

    @PutMapping("/{id}")
    public Paciente modificar(@PathVariable Long id, @Valid @RequestBody PacienteDTO dto) {
        return pacienteService.modificar(id, dto);
    }

    @PatchMapping("/{id}/estado")
    public Paciente cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        return pacienteService.cambiarEstado(id, activo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pacienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
