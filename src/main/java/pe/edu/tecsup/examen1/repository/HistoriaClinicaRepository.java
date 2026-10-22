package pe.edu.tecsup.examen1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.tecsup.examen1.entity.HistoriaClinica;

import java.util.List;

public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Long> {

    List<HistoriaClinica> findAllByOrderByFechaDesc();

    boolean existsByPaciente_Id(Long pacienteId);
}
