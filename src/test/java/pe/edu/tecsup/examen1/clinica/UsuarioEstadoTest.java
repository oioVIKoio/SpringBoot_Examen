package pe.edu.tecsup.examen1.clinica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.tecsup.examen1.entity.Usuario;
import pe.edu.tecsup.examen1.repository.AuditoriaRepository;
import pe.edu.tecsup.examen1.repository.UsuarioRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Un usuario no puede desactivar su propia cuenta
@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "admin", authorities = {"USUARIOS_VER", "USUARIOS_ESTADO"})
class UsuarioEstadoTest {

    private static final String MENSAJE = "No puedes desactivar tu propia cuenta";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    private Usuario admin;
    private Usuario doctor;

    @BeforeEach
    void crearDatos() {
        admin = crearUsuario("admin", "00000001", "admin@clinica.com");
        doctor = crearUsuario("cramirez", "00000002", "cramirez@clinica.com");
    }

    @Test
    void noPuedeDesactivarseDesdeLaVista() throws Exception {
        mockMvc.perform(get("/usuarios/desactivar/" + admin.getId()))
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attribute("error", MENSAJE));

        assertThat(usuarioRepository.findById(admin.getId()).orElseThrow().isActivo()).isTrue();
        assertThat(auditoriaRepository.findAll()).isEmpty();
    }

    @Test
    void noPuedeDesactivarseDesdeLaApi() throws Exception {
        mockMvc.perform(patch("/api/usuarios/" + admin.getId() + "/estado").param("activo", "false"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(MENSAJE)));

        assertThat(usuarioRepository.findById(admin.getId()).orElseThrow().isActivo()).isTrue();
    }

    @Test
    void puedeDesactivarAOtroUsuario() throws Exception {
        mockMvc.perform(get("/usuarios/desactivar/" + doctor.getId()))
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attributeCount(0));

        assertThat(usuarioRepository.findById(doctor.getId()).orElseThrow().isActivo()).isFalse();
    }

    @Test
    void laListaNoMuestraElBotonDeSuPropiaCuenta() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("/usuarios/desactivar/" + admin.getId() + "\""))))
                .andExpect(content().string(containsString("/usuarios/desactivar/" + doctor.getId() + "\"")));
    }

    private Usuario crearUsuario(String usuario, String dni, String correo) {
        Usuario nuevo = new Usuario();
        nuevo.setNombres("Nombre");
        nuevo.setApellidos("Apellido");
        nuevo.setDni(dni);
        nuevo.setCorreo(correo);
        nuevo.setUsuario(usuario);
        nuevo.setPassword("x");
        return usuarioRepository.save(nuevo);
    }
}
