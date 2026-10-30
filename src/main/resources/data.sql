-- Datos iniciales de la demo. Se cargan al arrancar y no pisan lo que ya exista.
-- Claves de prueba en el README.

INSERT IGNORE INTO roles (nombre, descripcion, activo) VALUES
('ADMINISTRADOR', 'Acceso total al sistema', 1),
('MEDICO', 'Atiende citas e historias clínicas', 1),
('RECEPCIONISTA', 'Registra pacientes y programa citas', 1);

INSERT IGNORE INTO permisos (nombre, descripcion) VALUES
('USUARIOS_VER', 'Permite consultar usuarios'),
('USUARIOS_CREAR', 'Permite registrar usuarios'),
('USUARIOS_EDITAR', 'Permite modificar usuarios'),
('USUARIOS_ESTADO', 'Permite activar o desactivar usuarios'),
('USUARIOS_ROLES', 'Permite asignar roles a usuarios'),
('ROLES_VER', 'Permite consultar roles'),
('ROLES_CREAR', 'Permite registrar roles'),
('ROLES_EDITAR', 'Permite modificar roles'),
('ROLES_PERMISOS', 'Permite asignar permisos a roles'),
('ROLES_ESTADO', 'Activar y desactivar roles'),
('PERMISOS_VER', 'Permite consultar permisos'),
('PERMISOS_CREAR', 'Permite registrar permisos'),
('PERMISOS_EDITAR', 'Permite modificar permisos'),
('PACIENTES_VER', 'Ver pacientes'),
('PACIENTES_CREAR', 'Crear pacientes'),
('PACIENTES_EDITAR', 'Editar pacientes'),
('CITAS_VER', 'Ver citas'),
('CITAS_CREAR', 'Crear citas'),
('CITAS_EDITAR', 'Editar citas'),
('HISTORIAS_VER', 'Ver historias clínicas'),
('HISTORIAS_CREAR', 'Crear historias clínicas'),
('HISTORIAS_EDITAR', 'Editar historias clínicas');

INSERT IGNORE INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p
WHERE r.nombre = 'ADMINISTRADOR'
   OR (r.nombre = 'MEDICO' AND (p.nombre LIKE 'PACIENTES\_%' OR p.nombre LIKE 'HISTORIAS\_%'))
   OR (r.nombre = 'RECEPCIONISTA' AND (p.nombre LIKE 'PACIENTES\_%' OR p.nombre LIKE 'CITAS\_%'));

INSERT IGNORE INTO usuarios
(nombres, apellidos, dni, correo, telefono, usuario, password, area, activo, fecha_registro, intentos_fallidos) VALUES
('Admin', 'Sistema', '00000001', 'admin@clinica.com', NULL, 'admin', '$2a$10$3FvXxrDa8Hrv05xd52LDs.YSzSlmyV2aqgCvSR32imDTD15M34fJy', NULL, 1, NOW(), 0),
('Carlos', 'Ramírez Soto', '45678912', 'carlos.ramirez@clinica.com', '999888777', 'cramirez', '$2a$10$h1HAVxFbePCGFYVvJ0nIhui2N33kUUjAkuuKRQ6Rv7w/qYnVmOa2C', 'Medicina general', 1, NOW(), 0),
('Lucía', 'Rojas Díaz', '70000010', 'lrojas@clinica.com', '987654321', 'lrojas', '$2a$10$HeiJrmX47ZRwtqRPQTFbIu/r0lpdWFY6DeEfR8fA3k/IhZVgo59Ci', 'Admisión', 1, NOW(), 0);

INSERT IGNORE INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id FROM usuarios u JOIN roles r
WHERE (u.usuario = 'admin' AND r.nombre = 'ADMINISTRADOR')
   OR (u.usuario = 'cramirez' AND r.nombre = 'MEDICO')
   OR (u.usuario = 'lrojas' AND r.nombre = 'RECEPCIONISTA');

INSERT IGNORE INTO pacientes (dni, nombres, apellidos, fecha_nacimiento, telefono, activo) VALUES
('71234567', 'Luis', 'Pérez Gómez', '1990-05-10', '987654321', 1);

INSERT INTO citas (paciente_id, medico_id, fecha_hora, motivo, estado)
SELECT p.id, m.id, '2026-11-03 09:30:00', 'Control de presión arterial', 'ATENDIDA'
FROM pacientes p JOIN usuarios m ON m.usuario = 'cramirez'
WHERE p.dni = '71234567'
  AND NOT EXISTS (SELECT 1 FROM citas c WHERE c.paciente_id = p.id);

INSERT IGNORE INTO historias_clinicas (paciente_id, diagnostico, tratamiento, fecha)
SELECT p.id, 'Hipertensión arterial controlada', 'Dieta baja en sodio y control mensual', '2026-10-22'
FROM pacientes p WHERE p.dni = '71234567';
