package com.juanamaris.inmobiliaria.service;

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

    /** Universidades agrupadas por ciudad, en orden alfabético (para los menús desplegables). */
    public Map<String, List<Universidad>> porCiudad() {
        return repo.findAllByOrderByCiudadAscNombreAsc().stream()
                .collect(Collectors.groupingBy(Universidad::getCiudad, TreeMap::new, Collectors.toList()));
    }

    public java.util.List<com.juanamaris.inmobiliaria.model.PuntoMapa> puntos() {
        return repo.findAllByOrderByCiudadAscNombreAsc().stream().filter(Universidad::isUbicada)
                .map(com.juanamaris.inmobiliaria.model.PuntoMapa::de).toList();
    }

    public Universidad buscar(Long id) { return repo.findById(id).orElse(null); }

    /** universidadId → cantidad de inmuebles activos cerca. */
    public Map<Long, Long> inmueblesCercanos() {
        Map<Long, Long> m = new HashMap<>();
        for (Object[] fila : repo.contarInmueblesCercanos()) m.put((Long) fila[0], (Long) fila[1]);
        return m;
    }
}
