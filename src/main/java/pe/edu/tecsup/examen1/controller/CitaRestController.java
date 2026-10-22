package pe.edu.tecsup.examen1.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.tecsup.examen1.dto.CitaDTO;
import pe.edu.tecsup.examen1.entity.Cita;
import pe.edu.tecsup.examen1.service.CitaService;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaRestController {

    private final CitaService citaService;

    public CitaRestController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    public List<Cita> listar() {
        return citaService.listar();
    }

    @GetMapping("/{id}")
    public Cita obtener(@PathVariable Long id) {
        return citaService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody CitaDTO dto) {
        return ResponseEntity.status(201).body(citaService.registrar(dto));
    }

    @PutMapping("/{id}")
    public Cita modificar(@PathVariable Long id, @Valid @RequestBody CitaDTO dto) {
        return citaService.modificar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        citaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
