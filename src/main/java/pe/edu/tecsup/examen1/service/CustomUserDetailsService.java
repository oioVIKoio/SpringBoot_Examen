package pe.edu.tecsup.examen1.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.tecsup.examen1.entity.Usuario;
import pe.edu.tecsup.examen1.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository
                .findByUsuarioOrCorreo(username, username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado: " + username
                        ));

        boolean bloqueado = false;

        if (usuario.getBloqueadoHasta() != null) {

            if (usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {

                bloqueado = true;

            } else {

                usuario.setBloqueadoHasta(null);
                usuario.setIntentosFallidos(0);

                usuarioRepository.save(usuario);
            }
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        for (var rol : usuario.getRoles()) {

            // Un rol inactivo no da acceso
            if (!rol.isActivo()) {
                continue;
            }

            authorities.add(
                    new SimpleGrantedAuthority(
                            "ROLE_" + rol.getNombre()
                    )
            );

            for (var permiso : rol.getPermisos()) {

                authorities.add(
                        new SimpleGrantedAuthority(
                                permiso.getNombre()
                        )
                );
            }
        }

        return User.builder()
                .username(usuario.getUsuario())
                .password(usuario.getPassword())
                .disabled(!usuario.isActivo())
                .accountLocked(bloqueado)
                .authorities(authorities)
                .build();
    }
}