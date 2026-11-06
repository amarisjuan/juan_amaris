package com.juanamaris.inmobiliaria.service;

import com.juanamaris.inmobiliaria.model.PuntoMapa;
import com.juanamaris.inmobiliaria.model.Universidad;
import com.juanamaris.inmobiliaria.repository.UniversidadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UniversidadService {

    private final UniversidadRepository repo;

    public UniversidadService(UniversidadRepository repo) { this.repo = repo; }

    /** Universidades agrupadas por ciudad, en orden alfabético (para las listas de la página de estudiantes). */
    public Map<String, List<Universidad>> porCiudad() {
        return repo.findAllByOrderByCiudadAscNombreAsc().stream()
                .collect(Collectors.groupingBy(Universidad::getCiudad, TreeMap::new, Collectors.toList()));
    }

    /** Ciudad -> universidades (id y nombre), para llenar el menú de universidades según la ciudad elegida. */
    public Map<String, List<Map<String, Object>>> mapaPorCiudad() {
        Map<String, List<Map<String, Object>>> m = new TreeMap<>();
        for (Universidad u : repo.findAllByOrderByCiudadAscNombreAsc()) {
            m.computeIfAbsent(u.getCiudad(), k -> new ArrayList<>())
                    .add(Map.of("id", u.getId(), "nombre", u.getNombreCorto(), "completo", u.getNombre()));
        }
        return m;
    }

    /** Universidades de una ciudad (vacía si no hay ninguna). */
    public List<Universidad> de(String ciudad) {
        if (ciudad == null || ciudad.isBlank()) return List.of();
        return repo.findAllByOrderByCiudadAscNombreAsc().stream().filter(u -> u.getCiudad().equalsIgnoreCase(ciudad.trim())).toList();
    }

    /** Departamentos que tienen universidades, en orden alfabético. */
    public List<String> departamentos() {
        return repo.findAll().stream().map(Universidad::getDepartamento).filter(Objects::nonNull)
                .distinct().sorted(Comparator.comparing(d -> GeografiaService.clave(d))).toList();
    }

    public List<PuntoMapa> puntos() {
        return repo.findAllByOrderByCiudadAscNombreAsc().stream().filter(Universidad::isUbicada)
                .map(PuntoMapa::de).toList();
    }

    public Universidad buscar(Long id) { return repo.findById(id).orElse(null); }

    /** universidadId → cantidad de inmuebles activos cerca. */
    public Map<Long, Long> inmueblesCercanos() {
        Map<Long, Long> m = new HashMap<>();
        for (Object[] fila : repo.contarInmueblesCercanos()) m.put((Long) fila[0], (Long) fila[1]);
        return m;
    }
}
