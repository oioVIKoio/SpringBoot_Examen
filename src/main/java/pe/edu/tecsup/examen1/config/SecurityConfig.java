package pe.edu.tecsup.examen1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import pe.edu.tecsup.examen1.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            LoginSuccessHandler loginSuccessHandler, LoginFailureHandler loginFailureHandler) {

        this.userDetailsService = userDetailsService;
        this.loginSuccessHandler = loginSuccessHandler;
        this.loginFailureHandler = loginFailureHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/registro",
                                "/recuperar-password",
                                "/restablecer-password",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()


                        .requestMatchers("/usuarios/nuevo")
                        .hasAuthority("USUARIOS_CREAR")

                        .requestMatchers("/usuarios/editar/**")
                        .hasAuthority("USUARIOS_EDITAR")

                        .requestMatchers(
                                "/usuarios/activar/**",
                                "/usuarios/desactivar/**"
                        )
                        .hasAuthority("USUARIOS_ESTADO")

                        .requestMatchers("/usuarios/roles/**")
                        .hasAuthority("USUARIOS_ROLES")

                        .requestMatchers("/usuarios/guardar")
                        .hasAnyAuthority(
                                "USUARIOS_CREAR",
                                "USUARIOS_EDITAR"
                        )

                        .requestMatchers(
                                "/usuarios",
                                "/usuarios/"
                        )
                        .hasAuthority("USUARIOS_VER")


                        .requestMatchers("/roles/nuevo")
                        .hasAuthority("ROLES_CREAR")

                        .requestMatchers("/roles/editar/**")
                        .hasAuthority("ROLES_EDITAR")

                        .requestMatchers("/roles/permisos/**")
                        .hasAuthority("ROLES_PERMISOS")

                        .requestMatchers(
                                "/roles/activar/**",
                                "/roles/desactivar/**"
                        )
                        .hasAuthority("ROLES_ESTADO")

                        .requestMatchers("/roles/guardar")
                        .hasAnyAuthority(
                                "ROLES_CREAR",
                                "ROLES_EDITAR"
                        )

                        .requestMatchers(
                                "/roles",
                                "/roles/"
                        )
                        .hasAuthority("ROLES_VER")


                        .requestMatchers("/permisos/nuevo")
                        .hasAuthority("PERMISOS_CREAR")

                        .requestMatchers("/permisos/editar/**")
                        .hasAuthority("PERMISOS_EDITAR")

                        .requestMatchers("/permisos/guardar")
                        .hasAnyAuthority(
                                "PERMISOS_CREAR",
                                "PERMISOS_EDITAR"
                        )

                        .requestMatchers(
                                "/permisos",
                                "/permisos/"
                        )
                        .hasAuthority("PERMISOS_VER")


                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/*/roles")
                        .hasAuthority("USUARIOS_ROLES")

                        .requestMatchers(HttpMethod.PATCH, "/api/usuarios/*/estado")
                        .hasAuthority("USUARIOS_ESTADO")

                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/*")
                        .hasAuthority("USUARIOS_ESTADO")

                        .requestMatchers(HttpMethod.POST, "/api/usuarios")
                        .hasAuthority("USUARIOS_CREAR")

                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/*")
                        .hasAuthority("USUARIOS_EDITAR")

                        .requestMatchers(HttpMethod.GET, "/api/usuarios/**")
                        .hasAuthority("USUARIOS_VER")


                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/roles/*/permisos"
                        )
                        .hasAuthority("ROLES_PERMISOS")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/roles"
                        )
                        .hasAuthority("ROLES_CREAR")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/roles/*"
                        )
                        .hasAuthority("ROLES_EDITAR")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/roles/**"
                        )
                        .hasAuthority("ROLES_VER")


                        // Pacientes: médico y recepcionista
                        .requestMatchers("/pacientes/nuevo")
                        .hasAuthority("PACIENTES_CREAR")

                        .requestMatchers(
                                "/pacientes/editar/**",
                                "/pacientes/activar/**",
                                "/pacientes/desactivar/**",
                                "/pacientes/eliminar/**"
                        )
                        .hasAuthority("PACIENTES_EDITAR")

                        .requestMatchers("/pacientes/guardar")
                        .hasAnyAuthority(
                                "PACIENTES_CREAR",
                                "PACIENTES_EDITAR"
                        )

                        .requestMatchers(HttpMethod.POST, "/api/pacientes")
                        .hasAuthority("PACIENTES_CREAR")

                        .requestMatchers(HttpMethod.PUT, "/api/pacientes/*")
                        .hasAuthority("PACIENTES_EDITAR")

                        .requestMatchers(HttpMethod.PATCH, "/api/pacientes/*/estado")
                        .hasAuthority("PACIENTES_EDITAR")

                        .requestMatchers(HttpMethod.DELETE, "/api/pacientes/*")
                        .hasAuthority("PACIENTES_EDITAR")

                        .requestMatchers(
                                "/pacientes",
                                "/pacientes/**",
                                "/api/pacientes",
                                "/api/pacientes/**"
                        )
                        .hasAuthority("PACIENTES_VER")


                        // Citas: recepcionista
                        .requestMatchers("/citas/nuevo")
                        .hasAuthority("CITAS_CREAR")

                        .requestMatchers(
                                "/citas/editar/**",
                                "/citas/eliminar/**"
                        )
                        .hasAuthority("CITAS_EDITAR")

                        .requestMatchers("/citas/guardar")
                        .hasAnyAuthority(
                                "CITAS_CREAR",
                                "CITAS_EDITAR"
                        )

                        .requestMatchers(HttpMethod.POST, "/api/citas")
                        .hasAuthority("CITAS_CREAR")

                        .requestMatchers(HttpMethod.PUT, "/api/citas/*")
                        .hasAuthority("CITAS_EDITAR")

                        .requestMatchers(HttpMethod.DELETE, "/api/citas/*")
                        .hasAuthority("CITAS_EDITAR")

                        .requestMatchers(
                                "/citas",
                                "/citas/**",
                                "/api/citas",
                                "/api/citas/**"
                        )
                        .hasAuthority("CITAS_VER")


                        // Historias clínicas: médico
                        .requestMatchers("/historias/nuevo")
                        .hasAuthority("HISTORIAS_CREAR")

                        .requestMatchers("/historias/editar/**")
                        .hasAuthority("HISTORIAS_EDITAR")

                        .requestMatchers("/historias/guardar")
                        .hasAnyAuthority(
                                "HISTORIAS_CREAR",
                                "HISTORIAS_EDITAR"
                        )

                        .requestMatchers(HttpMethod.POST, "/api/historias")
                        .hasAuthority("HISTORIAS_CREAR")

                        .requestMatchers(HttpMethod.PUT, "/api/historias/*")
                        .hasAuthority("HISTORIAS_EDITAR")

                        .requestMatchers(
                                "/historias",
                                "/historias/**",
                                "/api/historias",
                                "/api/historias/**"
                        )
                        .hasAuthority("HISTORIAS_VER")

                        .requestMatchers("/auditorias/**")
                        .hasRole("ADMINISTRADOR")
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}