package pe.edu.tecsup.examen1.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.tecsup.examen1.dto.HistoriaClinicaDTO;
import pe.edu.tecsup.examen1.entity.HistoriaClinica;
import pe.edu.tecsup.examen1.entity.Paciente;
import pe.edu.tecsup.examen1.exception.RecursoNoEncontradoException;
import pe.edu.tecsup.examen1.repository.HistoriaClinicaRepository;

import java.util.List;

@Service
public class HistoriaClinicaService {

    private final HistoriaClinicaRepository historiaRepository;
    private final PacienteService pacienteService;

    public HistoriaClinicaService(HistoriaClinicaRepository historiaRepository,
                                  PacienteService pacienteService) {
        this.historiaRepository = historiaRepository;
        this.pacienteService = pacienteService;
    }

    public List<HistoriaClinica> listar() {
        return historiaRepository.findAllByOrderByFechaDesc();
    }

    public HistoriaClinica buscarPorId(Long id) {
        return historiaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Historia clínica no encontrada con ID: " + id));
    }

    // Una sola historia por paciente (relación 1 a 1)
    @Transactional
    public HistoriaClinica registrar(HistoriaClinicaDTO dto) {
        if (dto.getPacienteId() == null) {
            throw new IllegalArgumentException("Seleccione un paciente");
        }
        Paciente paciente = pacienteService.buscarPorId(dto.getPacienteId());
        if (historiaRepository.existsByPaciente_Id(paciente.getId())) {
            throw new IllegalArgumentException("El paciente " + paciente.getNombreCompleto() + " ya tiene historia clínica");
        }

        HistoriaClinica historia = new HistoriaClinica();
        historia.setPaciente(paciente);
        copiarDatos(dto, historia);
        return historiaRepository.save(historia);
    }

    // El paciente de una historia no cambia
    @Transactional
    public HistoriaClinica modificar(Long id, HistoriaClinicaDTO dto) {
        HistoriaClinica historia = buscarPorId(id);
        copiarDatos(dto, historia);
        return historiaRepository.save(historia);
    }

    public HistoriaClinicaDTO aDTO(HistoriaClinica historia) {
        HistoriaClinicaDTO dto = new HistoriaClinicaDTO();
        dto.setId(historia.getId());
        dto.setPacienteId(historia.getPaciente().getId());
        dto.setDiagnostico(historia.getDiagnostico());
        dto.setTratamiento(historia.getTratamiento());
        dto.setFecha(historia.getFecha());
        return dto;
    }

    private void copiarDatos(HistoriaClinicaDTO dto, HistoriaClinica historia) {
        historia.setDiagnostico(dto.getDiagnostico());
        historia.setTratamiento(dto.getTratamiento());
        historia.setFecha(dto.getFecha());
    }
}
