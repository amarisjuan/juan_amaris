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
    private final BloqueoService bloqueos;
    private final com.juanamaris.inmobiliaria.repository.UsuarioRepository usuarios;

    public RegistroAnuncianteService(RegistroAnuncianteRepository repo, BloqueoService bloqueos,
                                     com.juanamaris.inmobiliaria.repository.UsuarioRepository usuarios) {
        this.repo = repo;
        this.usuarios = usuarios;
        this.bloqueos = bloqueos;
    }

    /** true si el documento está vetado. En ese caso también se bloquea la cuenta que intentó usarlo. */
    @Transactional
    public boolean documentoVetado(Usuario u, String documento) {
        if (!bloqueos.documentoBloqueado(documento)) return false;
        // Se recarga dentro de la transacción: el usuario que llega del controlador no está conectado a la base
        if (!"ADMIN".equals(u.getRol())) usuarios.findById(u.getId()).ifPresent(x -> x.setBloqueado(true));
        return true;
    }

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
        usuarios.findById(u.getId()).ifPresent(x ->
                x.setEmpresa(datos.getTipoDocumento() == com.juanamaris.inmobiliaria.model.TipoDocumento.NIT));
    }

    /** Solo para el panel del administrador. */
    @Transactional(readOnly = true)
    public List<RegistroAnunciante> todos() { return repo.findAllByOrderByFechaRegistroDesc(); }
}
