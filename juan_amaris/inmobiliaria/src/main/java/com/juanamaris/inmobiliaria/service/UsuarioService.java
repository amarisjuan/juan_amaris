package com.juanamaris.inmobiliaria.service;

import com.juanamaris.inmobiliaria.model.Usuario;
import com.juanamaris.inmobiliaria.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Usuario u = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.withUsername(u.getEmail()).password(u.getPassword()).roles(u.getRol()).build();
    }

    public boolean existeEmail(String email) { return repo.existsByEmailIgnoreCase(email); }

    @Transactional
    public Usuario registrar(Usuario u, String passwordPlano) {
        u.setEmail(u.getEmail().trim().toLowerCase());
        u.setPassword(encoder.encode(passwordPlano));
        u.setRol("USER");
        u.setFechaRegistro(java.time.LocalDateTime.now());
        return repo.save(u);
    }

    public Usuario porEmail(String email) {
        return repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
