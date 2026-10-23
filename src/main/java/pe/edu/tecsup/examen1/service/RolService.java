package pe.edu.tecsup.examen1.service;

import pe.edu.tecsup.examen1.entity.Permiso;
import pe.edu.tecsup.examen1.entity.Rol;

import java.util.List;
import java.util.Set;

public interface RolService {
    List<Rol> listarTodos();
    Rol obtenerPorId(Long id);
    Rol guardarRol(Rol rol);
    Rol asignarPermisos(Long rolId, Set<Long> permisosIds);
    Rol cambiarEstado(Long id, boolean activo);
    List<Permiso> listarPermisos();


}