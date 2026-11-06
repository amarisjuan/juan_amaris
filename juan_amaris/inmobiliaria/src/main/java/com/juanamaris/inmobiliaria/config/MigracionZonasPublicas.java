package com.juanamaris.inmobiliaria.config;

import com.juanamaris.inmobiliaria.repository.InmuebleRepository;
import com.juanamaris.inmobiliaria.service.UbicacionPrivadaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Calcula la zona pública de los inmuebles que tienen ubicación exacta pero aún no tienen zona. */
@Component
@Order(10)
public class MigracionZonasPublicas implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MigracionZonasPublicas.class);
    private final InmuebleRepository inmuebles;
    private final UbicacionPrivadaService ubicacion;

    public MigracionZonasPublicas(InmuebleRepository inmuebles, UbicacionPrivadaService ubicacion) {
        this.inmuebles = inmuebles;
        this.ubicacion = ubicacion;
    }

    @Override
    @Transactional
    public void run(String... args) {
        var pendientes = inmuebles.findAll().stream()
                .filter(i -> i.isUbicado() && !i.isZonaPublicaLista()).toList();
        pendientes.forEach(ubicacion::asignarZona);
        inmuebles.saveAll(pendientes);
        if (!pendientes.isEmpty()) log.info("Zona pública calculada para {} inmuebles", pendientes.size());
    }
}
