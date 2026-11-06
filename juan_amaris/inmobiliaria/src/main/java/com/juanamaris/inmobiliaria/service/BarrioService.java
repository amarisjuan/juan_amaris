package com.juanamaris.inmobiliaria.service;

import com.juanamaris.inmobiliaria.model.Barrio;
import com.juanamaris.inmobiliaria.repository.BarrioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** Barrios por ciudad. Se alimenta con lo que escriben los anunciantes; el texto libre sigue permitido. */
@Service
public class BarrioService {

    private final BarrioRepository repo;

    public BarrioService(BarrioRepository repo) { this.repo = repo; }

    /** Guarda el barrio si es nuevo (sin repetir por mayúsculas o tildes). */
    @Transactional
    public void registrar(String ciudad, String barrio) {
        if (ciudad == null || barrio == null || barrio.isBlank()) return;
        String nombre = normalizar(barrio);
        if (nombre.length() > 80) return;
        String c = ciudad.trim();
        boolean existe = repo.findAllByOrderByCiudadAscNombreAsc().stream()
                .anyMatch(b -> b.getCiudad().equalsIgnoreCase(c) && GeografiaService.clave(b.getNombre()).equals(GeografiaService.clave(nombre)));
        if (!existe) repo.save(new Barrio(c, nombre));
    }

    /** Ciudad -> barrios conocidos, en orden alfabético. */
    @Transactional(readOnly = true)
    public Map<String, List<String>> mapaPorCiudad() {
        Map<String, List<String>> m = new TreeMap<>();
        for (Barrio b : repo.findAllByOrderByCiudadAscNombreAsc()) {
            m.computeIfAbsent(b.getCiudad(), k -> new ArrayList<>()).add(b.getNombre());
        }
        return m;
    }

    static String normalizar(String s) {
        String t = s.trim().replaceAll("\\s+", " ");
        if (t.isEmpty()) return t;
        StringBuilder sb = new StringBuilder();
        for (String w : t.toLowerCase().split(" ")) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1));
        }
        return sb.toString();
    }
}
