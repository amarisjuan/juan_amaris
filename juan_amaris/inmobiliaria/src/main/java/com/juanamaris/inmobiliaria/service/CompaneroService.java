package com.juanamaris.inmobiliaria.service;

import com.juanamaris.inmobiliaria.model.Companero;
import com.juanamaris.inmobiliaria.model.FiltroCompanero;
import com.juanamaris.inmobiliaria.model.Usuario;
import com.juanamaris.inmobiliaria.repository.CompaneroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class CompaneroService {

    /** Para evitar spam: cada estudiante puede tener pocas publicaciones a la vez. */
    public static final int MAX_POR_USUARIO = 3;

    private final CompaneroRepository repo;

    public CompaneroService(CompaneroRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public Page<Companero> buscar(FiltroCompanero filtro, int pagina) {
        return repo.findAll(CompaneroRepository.buscar(filtro),
                PageRequest.of(Math.max(pagina, 0), 12, Sort.by("fechaPublicacion").descending()));
    }

    @Transactional(readOnly = true)
    public List<Companero> deUsuario(Usuario u) { return repo.findByAutorOrderByFechaPublicacionDesc(u); }

    @Transactional(readOnly = true)
    public Companero activo(Long id) {
        return repo.findById(id).filter(Companero::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));
    }

    @Transactional
    public Companero publicar(Companero c, Usuario autor) {
        if (repo.findByAutorOrderByFechaPublicacionDesc(autor).size() >= MAX_POR_USUARIO) {
            throw new IllegalArgumentException("Puedes tener máximo " + MAX_POR_USUARIO
                    + " publicaciones. Elimina una para crear otra.");
        }
        c.setAutor(autor);
        return repo.save(c);
    }

    @Transactional
    public void cambiarEstado(Long id, Usuario u) {
        Companero c = propio(id, u);
        c.setActivo(!c.isActivo());
    }

    @Transactional
    public void eliminar(Long id, Usuario u) { repo.delete(propio(id, u)); }

    private Companero propio(Long id, Usuario u) {
        Companero c = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));
        if (!"ADMIN".equals(u.getRol()) && !c.getAutor().getId().equals(u.getId())) {
            throw new AccessDeniedException("Esta publicación no es tuya");
        }
        return c;
    }
}
