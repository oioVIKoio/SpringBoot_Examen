package pe.edu.tecsup.examen1.clinica;

import com.fasterxml.jackson.databind.JsonNode;
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
import pe.edu.tecsup.examen1.entity.Rol;
import pe.edu.tecsup.examen1.entity.Usuario;
import pe.edu.tecsup.examen1.repository.RolRepository;
import pe.edu.tecsup.examen1.repository.UsuarioRepository;

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// P1: CRUD de pacientes, citas e historias clínicas con sus relaciones, DTO validados y errores
@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@WithMockUser(username = "admin", authorities = {"ROLE_ADMINISTRADOR",
        "PACIENTES_VER", "PACIENTES_CREAR", "PACIENTES_EDITAR",
        "CITAS_VER", "CITAS_CREAR", "CITAS_EDITAR",
        "HISTORIAS_VER", "HISTORIAS_CREAR", "HISTORIAS_EDITAR"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ClinicaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Long medicoId;
    private Long noMedicoId;

    @BeforeEach
    void crearMedico() {
        Rol rolMedico = new Rol();
        rolMedico.setNombre("MEDICO");
        rolRepository.save(rolMedico);

        medicoId = usuarioRepository.save(usuario("medico1", "10000001", Set.of(rolMedico))).getId();
        noMedicoId = usuarioRepository.save(usuario("recep1", "10000002", Set.of())).getId();
    }

    private Usuario usuario(String nombre, String dni, Set<Rol> roles) {
        Usuario u = new Usuario();
        u.setNombres("Ana");
        u.setApellidos("Torres");
        u.setDni(dni);
        u.setCorreo(nombre + "@clinica.com");
        u.setUsuario(nombre);
        u.setPassword("x");
        u.setRoles(roles);
        return u;
    }

    private Long crearPaciente(String dni) throws Exception {
        String json = """
                {"dni":"%s","nombres":"Luis","apellidos":"Pérez","fechaNacimiento":"1990-05-10","telefono":"987654321"}
                """.formatted(dni);
        String respuesta = mockMvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activo").value(true))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(respuesta).get("id").asLong();
    }

    private Long crearCita(Long pacienteId) throws Exception {
        String json = """
                {"pacienteId":%d,"medicoId":%d,"fechaHora":"2026-11-03T09:30:00","motivo":"Control"}
                """.formatted(pacienteId, medicoId);
        String respuesta = mockMvc.perform(post("/api/citas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paciente.dni").value("70000001"))
                .andExpect(jsonPath("$.medico.id").value(medicoId))
                .andExpect(jsonPath("$.medico.nombres").value("Ana"))
                .andExpect(jsonPath("$.medico.correo").doesNotExist())
                .andExpect(jsonPath("$.medico.roles").doesNotExist())
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(respuesta).get("id").asLong();
    }

    @Test
    void crudDePacienteConValidacionesYDniUnico() throws Exception {
        Long id = crearPaciente("70000001");

        mockMvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"123\",\"nombres\":\"\",\"apellidos\":\"X\",\"fechaNacimiento\":\"2999-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.dni").value("El DNI debe tener 8 dígitos"))
                .andExpect(jsonPath("$.nombres").exists())
                .andExpect(jsonPath("$.fechaNacimiento").value("La fecha de nacimiento debe ser pasada"));

        mockMvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"70000001\",\"nombres\":\"Otro\",\"apellidos\":\"X\",\"fechaNacimiento\":\"1990-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ya existe un paciente con el DNI 70000001"));

        mockMvc.perform(put("/api/pacientes/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"70000001\",\"nombres\":\"Luis Alberto\",\"apellidos\":\"Pérez\",\"fechaNacimiento\":\"1990-05-10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Luis Alberto"));

        mockMvc.perform(patch("/api/pacientes/" + id + "/estado").param("activo", "false"))
                .andExpect(jsonPath("$.activo").value(false));

        mockMvc.perform(get("/api/pacientes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Paciente no encontrado con ID: 999"));

        mockMvc.perform(delete("/api/pacientes/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/pacientes/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void citaUneUnPacienteConUnMedico() throws Exception {
        Long pacienteId = crearPaciente("70000001");
        Long citaId = crearCita(pacienteId);

        // Solo un usuario con el rol MEDICO puede atender
        mockMvc.perform(post("/api/citas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\":%d,\"medicoId\":%d,\"fechaHora\":\"2026-11-03T10:00:00\",\"motivo\":\"X\"}"
                                .formatted(pacienteId, noMedicoId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("no es un médico activo")));

        mockMvc.perform(put("/api/citas/" + citaId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\":%d,\"medicoId\":%d,\"fechaHora\":\"2026-11-03T11:00:00\",\"motivo\":\"Control\",\"estado\":\"ATENDIDA\"}"
                                .formatted(pacienteId, medicoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ATENDIDA"));

        // Integridad: un paciente con citas no se borra, se desactiva
        mockMvc.perform(delete("/api/pacientes/" + pacienteId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("desactívelo")));

        // Un paciente inactivo no recibe citas nuevas
        mockMvc.perform(patch("/api/pacientes/" + pacienteId + "/estado").param("activo", "false"));
        mockMvc.perform(post("/api/citas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\":%d,\"medicoId\":%d,\"fechaHora\":\"2026-11-04T09:00:00\",\"motivo\":\"X\"}"
                                .formatted(pacienteId, medicoId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("inactivo")));

        mockMvc.perform(delete("/api/citas/" + citaId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/pacientes/" + pacienteId)).andExpect(status().isNoContent());
    }

    @Test
    void historiaClinicaEsUnaPorPaciente() throws Exception {
        Long pacienteId = crearPaciente("70000001");
        String json = "{\"pacienteId\":%d,\"diagnostico\":\"Hipertensión\",\"tratamiento\":\"Dieta\",\"fecha\":\"2026-10-20\"}"
                .formatted(pacienteId);

        String respuesta = mockMvc.perform(post("/api/historias").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paciente.id").value(pacienteId))
                .andReturn().getResponse().getContentAsString();
        JsonNode historia = mapper.readTree(respuesta);

        mockMvc.perform(post("/api/historias").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("ya tiene historia clínica")));

        mockMvc.perform(put("/api/historias/" + historia.get("id").asLong()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"diagnostico\":\"Hipertensión controlada\",\"fecha\":\"2026-10-21\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnostico").value("Hipertensión controlada"))
                .andExpect(jsonPath("$.paciente.id").value(pacienteId));

        mockMvc.perform(delete("/api/pacientes/" + pacienteId)).andExpect(status().isBadRequest());
    }

    @Test
    void vistasMuestranValidacionesYErrores() throws Exception {
        mockMvc.perform(get("/pacientes/nuevo")).andExpect(status().isOk());
        mockMvc.perform(get("/citas/nuevo")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Seleccione un médico")));
        mockMvc.perform(get("/historias/nuevo")).andExpect(status().isOk());

        mockMvc.perform(post("/pacientes/guardar").param("dni", "12").param("nombres", "")
                        .param("apellidos", "Rojas").param("fechaNacimiento", "1995-02-02"))
                .andExpect(status().isOk())
                .andExpect(view().name("pacientes/formulario"))
                .andExpect(content().string(containsString("El DNI debe tener 8 dígitos")));

        Long pacienteId = crearPaciente("70000001");
        crearCita(pacienteId);

        mockMvc.perform(get("/pacientes")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Luis Pérez")));
        mockMvc.perform(get("/citas")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Control")));

        // La regla rota vuelve a la lista con el mensaje
        mockMvc.perform(post("/pacientes/eliminar/" + pacienteId))
                .andExpect(redirectedUrl("/pacientes"))
                .andExpect(flash().attribute("error", containsString("desactívelo")));

        mockMvc.perform(get("/citas/editar/999")).andExpect(status().isNotFound());
    }
}
