package com.juanamaris.inmobiliaria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SeguridadConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, java.time.Clock reloj) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        // Cualquiera puede buscar y ver inmuebles
                        .requestMatchers(HttpMethod.GET, "/", "/estudiantes", "/companeros", "/inmuebles/{id:\\d+}", "/fotos/**",
                                "/css/**", "/js/**", "/img/**", "/favicon.svg", "/webjars/**", "/registro", "/login", "/error",
                                "/seguridad", "/anunciantes/{id:\\d+}", "/recuperar", "/restablecer").permitAll()
                        .requestMatchers(HttpMethod.POST, "/registro", "/recuperar", "/restablecer").permitAll()
                        // Contar el clic de contacto de un inmueble es público (redirige a WhatsApp)
                        .requestMatchers(HttpMethod.GET, "/contacto/inmueble/**").permitAll()
                        // Panel y registro de anunciantes: solo administradores
                        .requestMatchers("/admin/**", "/admin").hasRole("ADMIN")
                        // Para publicar o editar hay que iniciar sesión
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").usernameParameter("email")
                        // Guarda la hora del ingreso: si después cambia la contraseña, esta sesión se cierra.
                        .successHandler(new org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler() {
                            { setDefaultTargetUrl("/mis-inmuebles"); }
                            @Override
                            public void onAuthenticationSuccess(jakarta.servlet.http.HttpServletRequest req,
                                    jakarta.servlet.http.HttpServletResponse res,
                                    org.springframework.security.core.Authentication auth)
                                    throws java.io.IOException, jakarta.servlet.ServletException {
                                req.getSession().setAttribute(SesionBloqueada.INGRESO, java.time.LocalDateTime.now(reloj));
                                super.onAuthenticationSuccess(req, res, auth);
                            }
                        })
                        // Cuenta bloqueada: mensaje propio, sin revelar el motivo
                        .failureHandler((req, res, ex) -> res.sendRedirect(req.getContextPath()
                                + (ex instanceof org.springframework.security.authentication.DisabledException ? "/login?bloqueado" : "/login?error")))
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll());
        return http.build();
    }
}
