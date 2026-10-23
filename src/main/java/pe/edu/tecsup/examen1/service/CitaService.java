package pe.edu.tecsup.examen1.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.tecsup.examen1.dto.CitaDTO;
import pe.edu.tecsup.examen1.entity.Cita;
import pe.edu.tecsup.examen1.entity.EstadoCita;
import pe.edu.tecsup.examen1.entity.Paciente;
import pe.edu.tecsup.examen1.entity.Usuario;
import pe.edu.tecsup.examen1.exception.RecursoNoEncontradoException;
import pe.edu.tecsup.examen1.repository.CitaRepository;
import pe.edu.tecsup.examen1.repository.UsuarioRepository;

import java.util.List;

@Service
public class CitaService {

    public static final String ROL_MEDICO = "MEDICO";

    private final CitaRepository citaRepository;
    private final PacienteService pacienteService;
    private final UsuarioRepository usuarioRepository;

    public CitaService(CitaRepository citaRepository,
                       PacienteService pacienteService,
                       UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.pacienteService = pacienteService;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Cita> listar() {
        return citaRepository.findAllByOrderByFechaHoraDesc();
    }

    public List<Usuario> listarMedicos() {
        return usuarioRepository.findDistinctByRoles_NombreAndRoles_ActivoAndActivo(ROL_MEDICO, true, true);
    }

    public Cita buscarPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada con ID: " + id));
    }

    @Transactional
    public Cita registrar(CitaDTO dto) {
        Cita cita = new Cita();
        copiarDatos(dto, cita);
        return citaRepository.save(cita);
    }

    @Transactional
    public Cita modificar(Long id, CitaDTO dto) {
        Cita cita = buscarPorId(id);
        copiarDatos(dto, cita);
        return citaRepository.save(cita);
    }

    @Transactional
    public Cita eliminar(Long id) {
        Cita cita = buscarPorId(id);
        citaRepository.delete(cita);
        return cita;
    }

    public CitaDTO aDTO(Cita cita) {
        CitaDTO dto = new CitaDTO();
        dto.setId(cita.getId());
        dto.setPacienteId(cita.getPaciente().getId());
        dto.setMedicoId(cita.getMedico().getId());
        dto.setFechaHora(cita.getFechaHora());
        dto.setMotivo(cita.getMotivo());
        dto.setEstado(cita.getEstado());
        return dto;
    }

    private void copiarDatos(CitaDTO dto, Cita cita) {
        // Al editar se puede conservar el paciente aunque ya esté inactivo; no se le asignan citas nuevas
        boolean mismoPaciente = cita.getPaciente() != null && dto.getPacienteId().equals(cita.getPaciente().getId());

        Paciente paciente = pacienteService.buscarPorId(dto.getPacienteId());
        if (!paciente.isActivo() && !mismoPaciente) {
            throw new IllegalArgumentException("El paciente " + paciente.getNombreCompleto() + " está inactivo");
        }

        Usuario medico = usuarioRepository.findById(dto.getMedicoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado con ID: " + dto.getMedicoId()));
        boolean esMedico = medico.getRoles().stream().anyMatch(r -> ROL_MEDICO.equals(r.getNombre()) && r.isActivo());
        if (!esMedico || !medico.isActivo()) {
            throw new IllegalArgumentException("El usuario " + medico.getUsuario() + " no es un médico activo");
        }

        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFechaHora(dto.getFechaHora());
        cita.setMotivo(dto.getMotivo());
        cita.setEstado(dto.getEstado() != null ? dto.getEstado() : EstadoCita.PROGRAMADA);
    }
}
