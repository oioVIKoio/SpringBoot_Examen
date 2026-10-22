package pe.edu.tecsup.examen1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.tecsup.examen1.entity.Paciente;

import java.util.List;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Long id);

    List<Paciente> findByActivoOrderByApellidosAsc(boolean activo);

    @Query("select p from Paciente p where not exists "
            + "(select h from HistoriaClinica h where h.paciente = p) order by p.apellidos")
    List<Paciente> findSinHistoria();
}
