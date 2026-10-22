package pe.edu.tecsup.examen1.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.tecsup.examen1.dto.HistoriaClinicaDTO;
import pe.edu.tecsup.examen1.entity.HistoriaClinica;
import pe.edu.tecsup.examen1.service.HistoriaClinicaService;

import java.util.List;

@RestController
@RequestMapping("/api/historias")
public class HistoriaClinicaRestController {

    private final HistoriaClinicaService historiaService;

    public HistoriaClinicaRestController(HistoriaClinicaService historiaService) {
        this.historiaService = historiaService;
    }

    @GetMapping
    public List<HistoriaClinica> listar() {
        return historiaService.listar();
    }

    @GetMapping("/{id}")
    public HistoriaClinica obtener(@PathVariable Long id) {
        return historiaService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody HistoriaClinicaDTO dto) {
        return ResponseEntity.status(201).body(historiaService.registrar(dto));
    }

    @PutMapping("/{id}")
    public HistoriaClinica modificar(@PathVariable Long id, @Valid @RequestBody HistoriaClinicaDTO dto) {
        return historiaService.modificar(id, dto);
    }
}
