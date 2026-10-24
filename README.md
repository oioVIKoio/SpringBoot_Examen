# SpringBoot_Examen_1

Proyecto desarrollado en **Spring Boot** para el módulo de **Login, Usuarios, Roles y Permisos**.

## Tecnologías

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
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

## Relaciones JPA

Se implementaron las relaciones solicitadas en el laboratorio:

- `@OneToMany` / `@ManyToOne`: Usuario - Auditoria.
- `@OneToOne`: TokenRecuperacion - Usuario.
- `@ManyToMany`: Usuario - Rol.
- `@ManyToMany`: Rol - Permiso.

## Base de datos

Durante el desarrollo se utilizaron diferentes entornos según cada integrante:

- MySQL.
- MariaDB mediante Docker.
- XAMPP.
- SQLyog para administración de MySQL/MariaDB.

La versión integrada del proyecto fue probada utilizando MariaDB.

## Ejecución

Copiar `src/main/resources/application-local.properties.example` como `application-local.properties` y poner los datos de la BD local (ese archivo no se sube al repo).

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
