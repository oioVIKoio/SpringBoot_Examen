# SpringBoot_Examen

Proyecto desarrollado en **Spring Boot** para el módulo de **Login, Usuarios, Roles y Permisos** (Evaluación 01) y el **Sistema Web de Gestión Hospitalaria** (Evaluación 02).

## Tecnologías

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- Spring AOP
- Thymeleaf
- Maven
- MySQL / MariaDB
- Docker
- XAMPP y SQLyog como herramientas utilizadas por integrantes del equipo

## Arquitectura

El proyecto aplica una arquitectura en capas:

```text
Controller → Service → Repository → Base de datos
```

Se utiliza **IoC e Inyección de Dependencias** mediante Spring.

## Requerimientos implementados

### Junior
- RF-LOGIN-01: Inicio de sesión con usuario o correo.
- RF-LOGIN-02: Validación de credenciales.
- RF-LOGIN-03: Manejo de credenciales incorrectas.
- RF-LOGIN-04: Recuperación de contraseña.

### Dávila
- RF-LOGIN-05: Cierre de sesión.
- RF-USR-01: Registro de usuarios.
- RF-USR-02: Modificación de usuarios.
- RF-USR-03: Activación y desactivación de usuarios.

### Panez
- RF-USR-04: Asignación de roles.
- RF-USR-05: Consulta y búsqueda de usuarios.
- RF-ROL-01: Registro y modificación de roles.
- RF-ROL-02: Asignación de permisos a roles.

### Victor
- RF-ROL-03: Control de acceso mediante permisos.
- RF-ROL-04: Restricción de funcionalidades no autorizadas.
- RF-AUD-01: Registro de operaciones críticas.
- RF-AUD-02: Consulta del historial de auditoría.

## Implementación

El sistema cuenta con:

- API REST con operaciones CRUD.
- Autenticación y autorización con Spring Security.
- Gestión de usuarios, roles y permisos.
- Recuperación de contraseña.
- Registro público de cuenta (`/registro`): la cuenta se crea sin rol y el administrador se lo asigna.
- Bloqueo temporal por intentos fallidos.
- Auditoría de operaciones.
- Control de sesión por inactividad.
- Persistencia mediante JPA e Hibernate.

## Evaluación 02: Gestión Hospitalaria

Versión entregada: [`v2.0.0`](https://github.com/oioVIKoio/SpringBoot_Examen/tree/v2.0.0).

| Pregunta | Qué se implementó |
|---|---|
| P1. Relaciones JPA | Módulos de `Paciente`, `Cita` e `HistoriaClinica` con su CRUD (vistas Thymeleaf y API REST), DTO con `@Valid` y `@RestControllerAdvice` |
| P2. Auditoría automática | `AuditoriaAspect` (AOP con `@AfterReturning`) registra usuario, fecha, operación, entidad y el id del registro al registrar, modificar, eliminar, cambiar estado o asignar |
| P3 y P4. Roles activo/inactivo | Los roles se activan y desactivan; un rol inactivo no da acceso. Rol `ADMINISTRADOR` |
| P5. Acceso por rol | Permisos en `SecurityConfig`, menú con `sec:authorize` y redirección según el rol al iniciar sesión (médico → `/historias`, recepcionista → `/citas`) |

### Endpoints de la clínica

```http
GET/POST         /api/pacientes
GET/PUT/DELETE   /api/pacientes/{id}
GET/POST         /api/citas
GET/PUT/DELETE   /api/citas/{id}
GET/POST         /api/historias
GET/PUT          /api/historias/{id}
```

Vistas: `/pacientes`, `/citas`, `/historias` y `/auditorias`.

## Relaciones JPA

Se implementaron las relaciones solicitadas en el laboratorio:

- `@OneToMany` / `@ManyToOne`: Usuario - Auditoria.
- `@OneToOne`: TokenRecuperacion - Usuario.
- `@ManyToMany`: Usuario - Rol.
- `@ManyToMany`: Rol - Permiso.
- `@OneToMany` / `@ManyToOne`: Paciente - Cita.
- `@ManyToOne`: Cita - Usuario (médico).
- `@OneToOne`: Paciente - HistoriaClinica.

## Base de datos

Durante el desarrollo se utilizaron diferentes entornos según cada integrante:

- MySQL.
- MariaDB mediante Docker.
- XAMPP.
- SQLyog para administración de MySQL/MariaDB.

La versión integrada del proyecto fue probada utilizando MariaDB.

## Ejecución

Copiar `src/main/resources/application-local.properties.example` como `application-local.properties` y poner los datos de la BD local (ese archivo no se sube al repo). La base de datos se crea sola si no existe.

Al arrancar, `data.sql` carga los roles, los permisos, un paciente con su cita e historia, y estos usuarios de prueba:

| Usuario | Clave | Rol |
|---|---|---|
| `admin` | `Admin2026x` | ADMINISTRADOR |
| `cramirez` | `Medico2026x` | MEDICO |
| `lrojas` | `Recepcion2026x` | RECEPCIONISTA |

Si esos datos ya existen no se modifican; si se borran, vuelven al reiniciar la aplicación.

```bash
mvn clean test
mvn spring-boot:run
```

La aplicación se ejecuta por defecto en:

```text
http://localhost:8080
```

## Repositorio

https://github.com/oioVIKoio/SpringBoot_Examen
