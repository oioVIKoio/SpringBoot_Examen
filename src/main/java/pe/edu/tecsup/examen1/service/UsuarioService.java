package pe.edu.tecsup.examen1.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.edu.tecsup.examen1.entity.TokenRecuperacion;
import pe.edu.tecsup.examen1.entity.Usuario;
import pe.edu.tecsup.examen1.repository.RolRepository;
import pe.edu.tecsup.examen1.repository.TokenRecuperacionRepository;
import pe.edu.tecsup.examen1.repository.UsuarioRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import pe.edu.tecsup.examen1.entity.Rol;
import java.util.HashSet;
import java.util.Set;
@Service
public class UsuarioService {

    private static final String MSG_USUARIO_NO_ENCONTRADO = "Usuario no encontrado con ID: ";
    private final RolRepository rolRepository;

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public UsuarioService(RolRepository rolRepository, UsuarioRepository usuarioRepository,
                          TokenRecuperacionRepository tokenRepository,
                          PasswordEncoder passwordEncoder,
                          Clock clock) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }
    public Usuario asignarRoles(Long usuarioId, Set<Long> rolesIds) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new RuntimeException(
                                MSG_USUARIO_NO_ENCONTRADO + usuarioId
                        )
                );

        List<Rol> roles = rolRepository.findAllById(rolesIds);

        if (roles.size() != rolesIds.size()) {
            throw new RuntimeException("Uno o más roles no existen");
        }

        usuario.setRoles(new HashSet<>(roles));

        Usuario actualizado = usuarioRepository.save(usuario);

        return actualizado;
    }
    private void validarPassword(String password) {

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres"
            );
        }

        boolean tieneMayuscula = false;
        boolean tieneMinuscula = false;
        boolean tieneNumero = false;

        for (char caracter : password.toCharArray()) {

            if (Character.isUpperCase(caracter)) {
                tieneMayuscula = true;
            }

            if (Character.isLowerCase(caracter)) {
                tieneMinuscula = true;
            }

            if (Character.isDigit(caracter)) {
                tieneNumero = true;
            }
        }

        if (!tieneMayuscula || !tieneMinuscula || !tieneNumero) {
            throw new IllegalArgumentException(
                    "La contraseña debe incluir mayúscula, minúscula y número"
            );
        }
    }
    private void validarDuplicados(Usuario usuario) {

        if (usuarioRepository.existsByUsuario(usuario.getUsuario())) {
            throw new IllegalArgumentException(
                    "El nombre de usuario ya está registrado"
            );
        }

        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new IllegalArgumentException(
                    "El correo ya está registrado"
            );
        }

        if (usuarioRepository.existsByDni(usuario.getDni())) {
            throw new IllegalArgumentException(
                    "El DNI ya está registrado"
            );
        }
    }

    public Usuario registrarUsuario(Usuario usuario) {

        validarDuplicados(usuario);
        validarPassword(usuario.getPassword());

        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );

        usuario.setActivo(true);

        Usuario registrado =
                usuarioRepository.save(usuario);

        return registrado;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
    public List<Usuario> buscarUsuarios(
            String termino,
            Long rolId,
            Boolean activo) {

        List<Usuario> usuarios;

        if (rolId != null && activo != null) {

            usuarios = usuarioRepository
                    .findDistinctByRoles_IdAndActivo(
                            rolId,
                            activo
                    );

        } else if (rolId != null) {

            usuarios = usuarioRepository
                    .findDistinctByRoles_Id(rolId);

        } else if (activo != null) {

            usuarios = usuarioRepository
                    .findByActivo(activo);

        } else {

            usuarios = usuarioRepository.findAll();
        }

        if (termino == null || termino.isBlank()) {
            return usuarios;
        }

        String texto = termino.trim().toLowerCase();

        return usuarios.stream()
                .filter(usuario ->
                        usuario.getNombres().toLowerCase().contains(texto)
                                || usuario.getApellidos().toLowerCase().contains(texto)
                                || usuario.getDni().contains(texto)
                                || usuario.getCorreo().toLowerCase().contains(texto)
                                || usuario.getUsuario().toLowerCase().contains(texto)
                )
                .toList();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(MSG_USUARIO_NO_ENCONTRADO + id));
    }

    public Usuario modificarUsuario(
            Long id,
            Usuario datosNuevos) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                MSG_USUARIO_NO_ENCONTRADO + id
                        )
                );

        usuario.setNombres(datosNuevos.getNombres());
        usuario.setApellidos(datosNuevos.getApellidos());
        usuario.setDni(datosNuevos.getDni());
        usuario.setCorreo(datosNuevos.getCorreo());
        usuario.setTelefono(datosNuevos.getTelefono());
        usuario.setUsuario(datosNuevos.getUsuario());
        usuario.setArea(datosNuevos.getArea());

        if (datosNuevos.getPassword() != null
                && !datosNuevos.getPassword().isBlank()) {

            validarPassword(datosNuevos.getPassword());

            usuario.setPassword(
                    passwordEncoder.encode(
                            datosNuevos.getPassword()
                    )
            );
        }

        Usuario actualizado =
                usuarioRepository.save(usuario);

        return actualizado;
    }

    public Usuario cambiarEstado(
            Long id,
            boolean activo) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                MSG_USUARIO_NO_ENCONTRADO + id
                        )
                );

        usuario.setActivo(activo);

        Usuario actualizado =
                usuarioRepository.save(usuario);

        return actualizado;
    }

    public Optional<Usuario> buscarPorUsuarioOCorreo(String termino) {
        return usuarioRepository.findByUsuarioOrCorreo(termino, termino);
    }

    public String crearTokenRecuperacion(Usuario usuario) {
        String token = UUID.randomUUID().toString();

        TokenRecuperacion tokenEntity = tokenRepository.findByUsuario(usuario)
                .orElse(new TokenRecuperacion());

        tokenEntity.setToken(token);
        tokenEntity.setUsuario(usuario);
        tokenEntity.setFechaExpiracion(
                LocalDateTime.now(clock).plusMinutes(15)
        );

        tokenRepository.save(tokenEntity);

        return token;
    }

    public boolean validarToken(String token) {
        Optional<TokenRecuperacion> tokenOpt = tokenRepository.findByToken(token);
        return tokenOpt.isPresent() && tokenOpt.get().getFechaExpiracion().isAfter(LocalDateTime.now(clock));
    }

    public boolean cambiarPasswordConToken(String token, String nuevaPassword) {
        Optional<TokenRecuperacion> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isPresent() && tokenOpt.get().getFechaExpiracion().isAfter(LocalDateTime.now(clock))) {
            TokenRecuperacion tokenEntity = tokenOpt.get();
            Usuario usuario = tokenEntity.getUsuario();

            validarPassword(nuevaPassword);

            usuario.setPassword(passwordEncoder.encode(nuevaPassword));

            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);

            usuarioRepository.save(usuario);

            tokenRepository.delete(tokenEntity);
            return true;
        }
        return false;
    }

}