package pe.edu.tecsup.examen1.clinica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.tecsup.examen1.entity.Auditoria;
import pe.edu.tecsup.examen1.entity.Permiso;
import pe.edu.tecsup.examen1.entity.Rol;
import pe.edu.tecsup.examen1.entity.Usuario;
import pe.edu.tecsup.examen1.repository.AuditoriaRepository;
import pe.edu.tecsup.examen1.repository.PermisoRepository;
import pe.edu.tecsup.examen1.repository.RolRepository;
import pe.edu.tecsup.examen1.repository.UsuarioRepository;
import pe.edu.tecsup.examen1.service.CitaService;
import pe.edu.tecsup.examen1.service.CustomUserDetailsService;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// P3 y P4: roles activos/inactivos
@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RolEstadoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PermisoRepository permisoRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CitaService citaService;

    private Rol medico;

    @BeforeEach
    void crearDatos() {
        Permiso verPacientes = new Permiso();
        verPacientes.setNombre("PACIENTES_VER");
        permisoRepository.save(verPacientes);

        medico = new Rol();
        medico.setNombre("MEDICO");
        medico.setDescripcion("Atiende citas");
        medico.setPermisos(new HashSet<>(Set.of(verPacientes)));
        medico = rolRepository.save(medico);

        Usuario admin = new Usuario();
        admin.setNombres("Admin");
        admin.setApellidos("Sistema");
        admin.setDni("00000001");
        admin.setCorreo("admin@clinica.com");
        admin.setUsuario("admin");
        admin.setPassword("x");
        usuarioRepository.save(admin);

        Usuario doctor = new Usuario();
        doctor.setNombres("Carlos");
        doctor.setApellidos("Ramírez");
        doctor.setDni("00000002");
        doctor.setCorreo("cramirez@clinica.com");
        doctor.setUsuario("cramirez");
        doctor.setPassword("x");
        doctor.setRoles(new HashSet<>(Set.of(medico)));
        usuarioRepository.save(doctor);
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLES_EDITAR")
    @Transactional
    void editarRolNoBorraSusPermisosNiSuEstado() throws Exception {
        medico.setActivo(false);
        rolRepository.save(medico);

        mockMvc.perform(post("/roles/guardar")
                        .param("id", medico.getId().toString())
                        .param("nombre", "MEDICO")
                        .param("descripcion", "Atiende citas y consultas"))
                .andExpect(redirectedUrl("/roles"));

        Rol editado = rolRepository.findById(medico.getId()).orElseThrow();
        assertThat(editado.getDescripcion()).isEqualTo("Atiende citas y consultas");
        assertThat(editado.getPermisos()).extracting(Permiso::getNombre).containsExactly("PACIENTES_VER");
        assertThat(editado.isActivo()).isFalse();
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLES_ESTADO")
    void desactivaYActivaUnRolYSeAudita() throws Exception {
        mockMvc.perform(get("/roles/desactivar/" + medico.getId())).andExpect(redirectedUrl("/roles"));
        assertThat(rolRepository.findById(medico.getId()).orElseThrow().isActivo()).isFalse();

        mockMvc.perform(get("/roles/activar/" + medico.getId())).andExpect(redirectedUrl("/roles"));
        assertThat(rolRepository.findById(medico.getId()).orElseThrow().isActivo()).isTrue();

        assertThat(auditoriaRepository.findAll())
                .extracting(Auditoria::getAccion, Auditoria::getEntidad, Auditoria::getEntidadId)
                .containsExactly(
                        tuple("DESACTIVAR", "Rol", medico.getId()),
                        tuple("ACTIVAR", "Rol", medico.getId()));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLES_VER")
    void sinElPermisoRolesEstadoNoCambiaElEstado() throws Exception {
        mockMvc.perform(get("/roles/desactivar/" + medico.getId())).andExpect(status().isForbidden());
        assertThat(rolRepository.findById(medico.getId()).orElseThrow().isActivo()).isTrue();
    }

    @Test
    void unRolInactivoNoDaAccesoNiCuentaComoMedico() {
        assertThat(userDetailsService.loadUserByUsername("cramirez").getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_MEDICO", "PACIENTES_VER");
        assertThat(citaService.listarMedicos()).extracting(Usuario::getUsuario).containsExactly("cramirez");

        medico.setActivo(false);
        rolRepository.save(medico);

        assertThat(userDetailsService.loadUserByUsername("cramirez").getAuthorities()).isEmpty();
        assertThat(citaService.listarMedicos()).isEmpty();
    }
}
