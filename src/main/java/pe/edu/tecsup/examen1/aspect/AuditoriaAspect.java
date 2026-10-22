package pe.edu.tecsup.examen1.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.examen1.service.AuditoriaService;

import java.util.Map;

// P2: auditoría automática de las operaciones que guardan, modifican o eliminan en los services
@Aspect
@Component
public class AuditoriaAspect {

    private static final Map<String, String> MODULOS = Map.of(
            "Usuario", "USUARIOS",
            "Rol", "ROLES",
            "Permiso", "PERMISOS",
            "Paciente", "PACIENTES",
            "Cita", "CITAS",
            "HistoriaClinica", "HISTORIAS_CLINICAS"
    );

    private final AuditoriaService auditoriaService;

    public AuditoriaAspect(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    // Todos los services menos el de auditoría (si no, se auditaría a sí mismo)
    @Pointcut("within(pe.edu.tecsup.examen1.service..*) "
            + "&& !within(pe.edu.tecsup.examen1.service.AuditoriaService)")
    public void servicios() {
    }

    @AfterReturning(pointcut = "servicios() && execution(* registrar*(..))", returning = "resultado")
    public void auditarRegistro(JoinPoint joinPoint, Object resultado) {
        registrar("REGISTRAR", "Se registró", joinPoint, resultado);
    }

    @AfterReturning(pointcut = "servicios() && execution(* modificar*(..))", returning = "resultado")
    public void auditarModificacion(JoinPoint joinPoint, Object resultado) {
        registrar("MODIFICAR", "Se modificó", joinPoint, resultado);
    }

    @AfterReturning(pointcut = "servicios() && execution(* eliminar*(..))", returning = "resultado")
    public void auditarEliminacion(JoinPoint joinPoint, Object resultado) {
        registrar("ELIMINAR", "Se eliminó", joinPoint, resultado);
    }

    @AfterReturning(pointcut = "servicios() && execution(* cambiarEstado*(..))", returning = "resultado")
    public void auditarCambioEstado(JoinPoint joinPoint, Object resultado) {
        boolean activo = (boolean) joinPoint.getArgs()[1];
        registrar(activo ? "ACTIVAR" : "DESACTIVAR", activo ? "Se activó" : "Se desactivó", joinPoint, resultado);
    }

    @AfterReturning(pointcut = "servicios() && execution(* asignar*(..))", returning = "resultado")
    public void auditarAsignacion(JoinPoint joinPoint, Object resultado) {
        String metodo = joinPoint.getSignature().getName();
        registrar(metodo.equals("asignarRoles") ? "ASIGNAR_ROLES" : "ASIGNAR_PERMISOS",
                "Se cambiaron los " + (metodo.equals("asignarRoles") ? "roles" : "permisos") + " de",
                joinPoint, resultado);
    }

    // guardar* registra o modifica: se mira si el objeto tenía id antes de ejecutar el método
    @Around("servicios() && execution(* guardar*(..))")
    public Object auditarGuardado(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean nuevo = obtenerId(joinPoint.getArgs()[0]) == null;
        Object resultado = joinPoint.proceed();
        registrar(nuevo ? "REGISTRAR" : "MODIFICAR", nuevo ? "Se registró" : "Se modificó", joinPoint, resultado);
        return resultado;
    }

    private void registrar(String accion, String verbo, JoinPoint joinPoint, Object resultado) {
        if (resultado == null) {
            return;
        }
        String entidad = resultado.getClass().getSimpleName();
        Long id = obtenerId(resultado);

        auditoriaService.registrar(
                accion,
                MODULOS.getOrDefault(entidad, entidad.toUpperCase()),
                verbo + " " + entidad + " ID " + id + " (" + joinPoint.getSignature().toShortString() + ")",
                entidad,
                id
        );
    }

    private Long obtenerId(Object entidad) {
        try {
            return (Long) entidad.getClass().getMethod("getId").invoke(entidad);
        } catch (ReflectiveOperationException | ClassCastException e) {
            return null;
        }
    }
}
