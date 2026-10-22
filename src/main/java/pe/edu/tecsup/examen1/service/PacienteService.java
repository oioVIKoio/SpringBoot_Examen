package pe.edu.tecsup.examen1.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.tecsup.examen1.dto.PacienteDTO;
import pe.edu.tecsup.examen1.entity.Paciente;
import pe.edu.tecsup.examen1.exception.RecursoNoEncontradoException;
import pe.edu.tecsup.examen1.repository.CitaRepository;
import pe.edu.tecsup.examen1.repository.HistoriaClinicaRepository;
import pe.edu.tecsup.examen1.repository.PacienteRepository;

import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;
    private final HistoriaClinicaRepository historiaRepository;

    public PacienteService(PacienteRepository pacienteRepository,
                           CitaRepository citaRepository,
                           HistoriaClinicaRepository historiaRepository) {
        this.pacienteRepository = pacienteRepository;
        this.citaRepository = citaRepository;
        this.historiaRepository = historiaRepository;
    }

    public List<Paciente> listar() {
        return pacienteRepository.findAll();
    }

    public List<Paciente> listarActivos() {
        return pacienteRepository.findByActivoOrderByApellidosAsc(true);
    }

    public List<Paciente> listarSinHistoria() {
        return pacienteRepository.findSinHistoria();
    }

    public Paciente buscarPorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado con ID: " + id));
    }

    @Transactional
    public Paciente registrar(PacienteDTO dto) {
        if (pacienteRepository.existsByDni(dto.getDni())) {
            throw new IllegalArgumentException("Ya existe un paciente con el DNI " + dto.getDni());
        }
        Paciente paciente = new Paciente();
        copiarDatos(dto, paciente);
        return pacienteRepository.save(paciente);
    }

    @Transactional
    public Paciente modificar(Long id, PacienteDTO dto) {
        Paciente paciente = buscarPorId(id);
        if (pacienteRepository.existsByDniAndIdNot(dto.getDni(), id)) {
            throw new IllegalArgumentException("Ya existe un paciente con el DNI " + dto.getDni());
        }
        copiarDatos(dto, paciente);
        return pacienteRepository.save(paciente);
    }

    @Transactional
    public Paciente cambiarEstado(Long id, boolean activo) {
        Paciente paciente = buscarPorId(id);
        paciente.setActivo(activo);
        return pacienteRepository.save(paciente);
    }

    // Integridad: con citas o historia clínica no se borra, se desactiva
    @Transactional
    public Paciente eliminar(Long id) {
        Paciente paciente = buscarPorId(id);
        if (citaRepository.existsByPaciente_Id(id) || historiaRepository.existsByPaciente_Id(id)) {
            throw new IllegalArgumentException("El paciente " + paciente.getNombreCompleto()
                    + " tiene citas o historia clínica: no se puede eliminar, desactívelo");
        }
        pacienteRepository.delete(paciente);
        return paciente;
    }

    public PacienteDTO aDTO(Paciente paciente) {
        PacienteDTO dto = new PacienteDTO();
        dto.setId(paciente.getId());
        dto.setDni(paciente.getDni());
        dto.setNombres(paciente.getNombres());
        dto.setApellidos(paciente.getApellidos());
        dto.setFechaNacimiento(paciente.getFechaNacimiento());
        dto.setTelefono(paciente.getTelefono());
        return dto;
    }

    private void copiarDatos(PacienteDTO dto, Paciente paciente) {
        paciente.setDni(dto.getDni());
        paciente.setNombres(dto.getNombres());
        paciente.setApellidos(dto.getApellidos());
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setTelefono(dto.getTelefono());
    }
}
