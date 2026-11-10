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
    public SecurityFilterChain filterChain(HttpSecurity http, java.time.Clock reloj,
                                           com.juanamaris.inmobiliaria.service.IntentosIngreso intentos) throws Exception {
        // Antes de revisar la contraseña: si hay demasiados intentos fallidos, ni se intenta.
        http.addFilterBefore(new org.springframework.web.filter.OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res,
                                            jakarta.servlet.FilterChain cadena) throws jakarta.servlet.ServletException, java.io.IOException {
                if ("POST".equals(req.getMethod()) && (req.getContextPath() + "/login").equals(req.getRequestURI())
                        && intentos.bloqueado(req.getParameter("email"), req.getRemoteAddr())) {
                    res.sendRedirect(req.getContextPath() + "/login?intentos");
                    return;
                }
                cadena.doFilter(req, res);
            }
        }, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
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
                                intentos.exito(auth.getName());
                                super.onAuthenticationSuccess(req, res, auth);
                            }
                        })
                        // Cuenta bloqueada: mensaje propio, sin revelar el motivo
                        .failureHandler((req, res, ex) -> {
                            boolean deshabilitada = ex instanceof org.springframework.security.authentication.DisabledException;
                            if (!deshabilitada) intentos.fallo(req.getParameter("email"), req.getRemoteAddr());
                            res.sendRedirect(req.getContextPath() + (deshabilitada ? "/login?bloqueado"
                                    : intentos.bloqueado(req.getParameter("email"), req.getRemoteAddr()) ? "/login?intentos" : "/login?error"));
                        })
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll());
        return http.build();
    }
}
