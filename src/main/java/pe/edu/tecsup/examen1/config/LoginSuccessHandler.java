package pe.edu.tecsup.examen1.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import pe.edu.tecsup.examen1.entity.Usuario;
import pe.edu.tecsup.examen1.repository.UsuarioRepository;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;

    public LoginSuccessHandler(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        String username = authentication.getName();

        Usuario usuario = usuarioRepository
                .findByUsuarioOrCorreo(username, username)
                .orElseThrow(() ->
                        new RuntimeException("Usuario autenticado no encontrado"));

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);

        usuarioRepository.save(usuario);

        String destino = "/";

        var authorities = authentication.getAuthorities();

        boolean esAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        boolean puedeVerUsuarios = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("USUARIOS_VER"));

        boolean puedeVerRoles = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLES_VER"));
        boolean puedeVerPermisos = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("PERMISOS_VER"));

        boolean esMedico = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MEDICO"));

        boolean esRecepcionista = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECEPCIONISTA"));

        if (esAdmin) {
            destino = "/";
        } else if (esMedico) {
            destino = "/historias";
        } else if (esRecepcionista) {
            destino = "/citas";
        } else if (puedeVerUsuarios) {
            destino = "/usuarios";
        } else if (puedeVerRoles) {
            destino = "/roles";
        } else if (puedeVerPermisos) {
            destino = "/permisos";
        }

        response.sendRedirect(destino);
    }
}