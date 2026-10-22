package pe.edu.tecsup.examen1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.tecsup.examen1.entity.Cita;

import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findAllByOrderByFechaHoraDesc();

    boolean existsByPaciente_Id(Long pacienteId);
}
