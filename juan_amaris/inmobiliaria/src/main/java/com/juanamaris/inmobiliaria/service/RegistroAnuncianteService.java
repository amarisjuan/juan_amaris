package com.juanamaris.inmobiliaria.service;

import com.juanamaris.inmobiliaria.model.RegistroAnunciante;
import com.juanamaris.inmobiliaria.model.Usuario;
import com.juanamaris.inmobiliaria.repository.RegistroAnuncianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Registro privado de anunciantes (Ley 1480 de 2011, art. 53). Solo para el dueño de los datos y el administrador. */
@Service
public class RegistroAnuncianteService {

    private final RegistroAnuncianteRepository repo;

    public RegistroAnuncianteService(RegistroAnuncianteRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public boolean tieneRegistro(Usuario u) { return repo.existsByUsuario(u); }

    @Transactional(readOnly = true)
    public Optional<RegistroAnunciante> de(Usuario u) { return repo.findByUsuario(u); }

    @Transactional
    public void guardar(Usuario u, RegistroAnunciante datos) {
        RegistroAnunciante r = repo.findByUsuario(u).orElseGet(() -> {
            RegistroAnunciante nuevo = new RegistroAnunciante();
            nuevo.setUsuario(u);
            return nuevo;
        });
        if (r.getId() != null) r.setFechaActualizacion(LocalDateTime.now());
        r.setTipoDocumento(datos.getTipoDocumento());
        r.setNumeroDocumento(datos.getNumeroDocumento());
        r.setDireccionNotificaciones(datos.getDireccionNotificaciones());
        r.setCiudadNotificaciones(datos.getCiudadNotificaciones());
        repo.save(r);
    }

    /** Solo para el panel del administrador. */
    @Transactional(readOnly = true)
    public List<RegistroAnunciante> todos() { return repo.findAllByOrderByFechaRegistroDesc(); }
}
