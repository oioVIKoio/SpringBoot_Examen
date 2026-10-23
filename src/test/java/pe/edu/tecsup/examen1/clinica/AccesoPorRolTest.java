package pe.edu.tecsup.examen1.clinica;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.tecsup.examen1.entity.Rol;
import pe.edu.tecsup.examen1.entity.Usuario;
import pe.edu.tecsup.examen1.repository.RolRepository;
import pe.edu.tecsup.examen1.repository.UsuarioRepository;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// P5: cada rol ve y usa solo sus módulos; el backend valida los permisos
@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AccesoPorRolTest {

    private static final String MEDICO = "ROLE_MEDICO";
    private static final String RECEPCIONISTA = "ROLE_RECEPCIONISTA";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(authorities = {MEDICO, "PACIENTES_VER", "PACIENTES_CREAR", "PACIENTES_EDITAR",
            "HISTORIAS_VER", "HISTORIAS_CREAR", "HISTORIAS_EDITAR"})
    void elMedicoEntraAPacientesEHistoriasPeroNoACitasNiUsuarios() throws Exception {
        mockMvc.perform(get("/pacientes")).andExpect(status().isOk());
        mockMvc.perform(get("/historias")).andExpect(status().isOk());
        mockMvc.perform(get("/api/historias")).andExpect(status().isOk());

        mockMvc.perform(get("/citas")).andExpect(status().isForbidden());
        mockMvc.perform(get("/citas/nuevo")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/citas")).andExpect(status().isForbidden());
        mockMvc.perform(get("/usuarios")).andExpect(status().isForbidden());
        mockMvc.perform(get("/auditorias")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {RECEPCIONISTA, "PACIENTES_VER", "PACIENTES_CREAR", "PACIENTES_EDITAR",
            "CITAS_VER", "CITAS_CREAR", "CITAS_EDITAR"})
    void laRecepcionistaEntraAPacientesYCitasPeroNoAHistorias() throws Exception {
        mockMvc.perform(get("/pacientes")).andExpect(status().isOk());
        mockMvc.perform(get("/citas")).andExpect(status().isOk());

        mockMvc.perform(get("/historias")).andExpect(status().isForbidden());
        mockMvc.perform(get("/historias/nuevo")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/historias").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"PACIENTES_VER"})
    void soloVerNoPermiteCrearEditarNiCambiarEstado() throws Exception {
        mockMvc.perform(get("/pacientes")).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Nuevo Paciente"))));

        mockMvc.perform(get("/pacientes/nuevo")).andExpect(status().isForbidden());
        mockMvc.perform(get("/pacientes/editar/1")).andExpect(status().isForbidden());
        mockMvc.perform(get("/pacientes/desactivar/1")).andExpect(status().isForbidden());
        mockMvc.perform(post("/pacientes/eliminar/1")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void despuesDelLoginCadaRolVaASuModulo() throws Exception {
        crearUsuario("doctor", "00000002", "MEDICO");
        crearUsuario("recepcion", "00000003", "RECEPCIONISTA");
        crearUsuario("jefe", "00000004", "ADMINISTRADOR");

        mockMvc.perform(formLogin().user("doctor").password("Clave2026x")).andExpect(redirectedUrl("/historias"));
        mockMvc.perform(formLogin().user("recepcion").password("Clave2026x")).andExpect(redirectedUrl("/citas"));
        mockMvc.perform(formLogin().user("jefe").password("Clave2026x")).andExpect(redirectedUrl("/"));
    }

    private void crearUsuario(String usuario, String dni, String nombreRol) {
        Rol rol = new Rol();
        rol.setNombre(nombreRol);
        rol = rolRepository.save(rol);

        Usuario u = new Usuario();
        u.setNombres(usuario);
        u.setApellidos("Prueba");
        u.setDni(dni);
        u.setCorreo(usuario + "@clinica.com");
        u.setUsuario(usuario);
        u.setPassword(passwordEncoder.encode("Clave2026x"));
        u.setRoles(new HashSet<>(Set.of(rol)));
        usuarioRepository.save(u);
    }

    @Test
    @WithMockUser(authorities = {MEDICO, "PACIENTES_VER", "HISTORIAS_VER"})
    void elMenuMuestraSoloLosModulosDelRol() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/pacientes\"")))
                .andExpect(content().string(containsString("href=\"/historias\"")))
                .andExpect(content().string(not(containsString("href=\"/citas\""))))
                .andExpect(content().string(not(containsString("href=\"/usuarios\""))))
                .andExpect(content().string(not(containsString("href=\"/auditorias\""))));
    }
}
