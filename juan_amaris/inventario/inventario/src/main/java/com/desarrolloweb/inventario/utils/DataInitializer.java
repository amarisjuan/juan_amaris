package com.desarrolloweb.inventario.utils;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.desarrolloweb.inventario.model.dao.RolDAO;
import com.desarrolloweb.inventario.model.dao.UsuarioDAO;
import com.desarrolloweb.inventario.model.entity.Rol;
import com.desarrolloweb.inventario.model.entity.Usuario;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner init(RolDAO rolDAO, UsuarioDAO usuarioDAO, PasswordEncoder encoder) {
        return args -> {
            // create roles if not present
            Rol admin = rolDAO.findByNombre("ROLE_ADMIN");
            if (admin == null) {
                admin = new Rol(1L, "ROLE_ADMIN");
                rolDAO.save(admin);
            }

            Rol user = rolDAO.findByNombre("ROLE_USER");
            if (user == null) {
                user = new Rol(2L, "ROLE_USER");
                rolDAO.save(user);
            }

            if (usuarioDAO.findByUsername("admin").isEmpty()) {
                Usuario u = new Usuario();
                u.setUsername("admin");
                u.setPassword(encoder.encode("admin"));
                u.setActivo(true);
                u.setRoles(Arrays.asList(admin, user));
                usuarioDAO.save(u);
            }

            if (usuarioDAO.findByUsername("user").isEmpty()) {
                Usuario u2 = new Usuario();
                u2.setUsername("user");
                u2.setPassword(encoder.encode("user"));
                u2.setActivo(true);
                u2.setRoles(Arrays.asList(user));
                usuarioDAO.save(u2);
            }
        };
    }
}
