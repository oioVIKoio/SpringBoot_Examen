package pe.edu.tecsup.examen1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.tecsup.examen1.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuarioOrCorreo(String usuario, String correo);
    List<Usuario> findByActivo(boolean activo);

    List<Usuario> findDistinctByRoles_Id(Long rolId);

    List<Usuario> findDistinctByRoles_IdAndActivo(Long rolId, boolean activo);

    List<Usuario> findDistinctByRoles_NombreAndActivo(String nombreRol, boolean activo);

    boolean existsByUsuario(String usuario);

    boolean existsByCorreo(String correo);

    boolean existsByDni(String dni);
}