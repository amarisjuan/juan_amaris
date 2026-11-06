package com.juanamaris.inmobiliaria.config;

import com.juanamaris.inmobiliaria.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Cuentas creadas antes de separar nombres y apellidos: se separan a partir del nombre guardado. */
@Component
@Order(5)
public class MigracionNombres implements CommandLineRunner {

    private final UsuarioRepository usuarios;

    public MigracionNombres(UsuarioRepository usuarios) { this.usuarios = usuarios; }

    @Override
    @Transactional
    public void run(String... args) {
        for (var u : usuarios.findAll()) {
            if (u.getNombres() != null && !u.getNombres().isBlank()) continue;
            String[] p = (u.getNombre() == null ? "Usuario" : u.getNombre().trim()).split("\\s+", 2);
            u.setNombres(p[0]);
            u.setApellidos(p.length > 1 ? p[1] : "-");
        }
    }
}
