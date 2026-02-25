package com.desarrolloweb.inventario.service;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.desarrolloweb.inventario.model.dao.UsuarioDAO;
import com.desarrolloweb.inventario.model.entity.Usuario;

@Service
public class JpaUsuarioDetailsService implements UserDetailsService {

    private final UsuarioDAO usuarioDAO;

    public JpaUsuarioDetailsService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioDAO.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .disabled(!usuario.isActivo())
                .authorities(usuario.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.getNombre())).collect(Collectors.toList()))
                .build();
    }
}
