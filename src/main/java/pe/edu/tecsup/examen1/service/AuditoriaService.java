package pe.edu.tecsup.examen1.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.tecsup.examen1.entity.Auditoria;
import pe.edu.tecsup.examen1.entity.Usuario;
import pe.edu.tecsup.examen1.repository.AuditoriaRepository;
import pe.edu.tecsup.examen1.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;



    public AuditoriaService(

            AuditoriaRepository auditoriaRepository,

            UsuarioRepository usuarioRepository) {

        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrar(
            String accion,
            String modulo,
            String descripcion,
            String entidad,
            Long entidadId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return;
        }

        String username = authentication.getName();

        Optional<Usuario> usuarioOpt =
                usuarioRepository.findByUsuarioOrCorreo(
                        username,
                        username
                );

        if (usuarioOpt.isEmpty()) {
            return;
        }

        Auditoria auditoria = new Auditoria();

        auditoria.setAccion(accion);
        auditoria.setModulo(modulo);
        auditoria.setDescripcion(descripcion);
        auditoria.setEntidad(entidad);
        auditoria.setEntidadId(entidadId);
        auditoria.setUsuario(usuarioOpt.get());

        auditoriaRepository.save(auditoria);
    }

    public List<Auditoria> listarTodas() {
        return auditoriaRepository.findAllByOrderByFechaDesc();
    }
}