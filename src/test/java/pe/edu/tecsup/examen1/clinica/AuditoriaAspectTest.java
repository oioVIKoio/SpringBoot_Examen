package pe.edu.tecsup.examen1.clinica;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.tecsup.examen1.entity.Auditoria;
import pe.edu.tecsup.examen1.entity.Rol;
import pe.edu.tecsup.examen1.entity.Usuario;
import pe.edu.tecsup.examen1.repository.AuditoriaRepository;
import pe.edu.tecsup.examen1.repository.UsuarioRepository;
import pe.edu.tecsup.examen1.service.RolService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// P2: el aspecto registra usuario, fecha, operación, entidad e id sin llamadas manuales
@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@WithMockUser(username = "admin")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuditoriaAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private RolService rolService;

    @BeforeEach
    void crearAdmin() {
        Usuario admin = new Usuario();
        admin.setNombres("Admin");
        admin.setApellidos("Sistema");
        admin.setDni("00000001");
        admin.setCorreo("admin@clinica.com");
        admin.setUsuario("admin");
        admin.setPassword("x");
        usuarioRepository.save(admin);
    }

    @Test
    void registraModificaDesactivaYEliminaUnPaciente() throws Exception {
        String json = "{\"dni\":\"70000001\",\"nombres\":\"Luis\",\"apellidos\":\"Pérez\",\"fechaNacimiento\":\"1990-05-10\"}";
        String respuesta = mockMvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(respuesta).get("id").asLong();

        mockMvc.perform(put("/api/pacientes/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(json.replace("Luis", "Luis Alberto"))).andExpect(status().isOk());
        mockMvc.perform(patch("/api/pacientes/" + id + "/estado").param("activo", "false"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/pacientes/" + id)).andExpect(status().isNoContent());

        // Una operación que falla no se audita
        mockMvc.perform(get("/api/pacientes/" + id)).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/pacientes/" + id)).andExpect(status().isNotFound());

        List<Auditoria> registros = auditoriaRepository.findAll();
        assertThat(registros).extracting(Auditoria::getAccion)
                .containsExactly("REGISTRAR", "MODIFICAR", "DESACTIVAR", "ELIMINAR");
        assertThat(registros).allSatisfy(a -> {
            assertThat(a.getUsuario().getUsuario()).isEqualTo("admin");
            assertThat(a.getFecha()).isNotNull();
            assertThat(a.getModulo()).isEqualTo("PACIENTES");
            assertThat(a.getEntidad()).isEqualTo("Paciente");
            assertThat(a.getEntidadId()).isEqualTo(id);
        });
    }

    @Test
    void guardarDistingueRegistroDeModificacion() {
        Rol rol = new Rol();
        rol.setNombre("MEDICO");
        Rol guardado = rolService.guardarRol(rol);

        guardado.setDescripcion("Atiende citas");
        rolService.guardarRol(guardado);

        assertThat(auditoriaRepository.findAll())
                .extracting(Auditoria::getAccion, Auditoria::getEntidad, Auditoria::getEntidadId)
                .containsExactly(
                        tuple("REGISTRAR", "Rol", guardado.getId()),
                        tuple("MODIFICAR", "Rol", guardado.getId()));
    }
}
