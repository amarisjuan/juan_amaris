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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        // Cualquiera puede buscar y ver inmuebles
                        .requestMatchers(HttpMethod.GET, "/", "/estudiantes", "/companeros", "/inmuebles/{id:\\d+}", "/fotos/**",
                                "/css/**", "/js/**", "/img/**", "/favicon.svg", "/webjars/**", "/registro", "/login", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/registro").permitAll()
                        // Contar el clic de contacto de un inmueble es público (redirige a WhatsApp)
                        .requestMatchers(HttpMethod.GET, "/contacto/inmueble/**").permitAll()
                        // Panel y registro de anunciantes: solo administradores
                        .requestMatchers("/admin/**", "/admin").hasRole("ADMIN")
                        // Para publicar o editar hay que iniciar sesión
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").usernameParameter("email")
                        .defaultSuccessUrl("/mis-inmuebles").permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll());
        return http.build();
    }
}
