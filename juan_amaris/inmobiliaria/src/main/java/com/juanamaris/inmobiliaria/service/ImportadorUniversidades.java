package com.juanamaris.inmobiliaria.service;

import com.juanamaris.inmobiliaria.model.Universidad;
import com.juanamaris.inmobiliaria.repository.UniversidadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Carga las instituciones de educación superior activas (SNIES, MEN) desde datos/instituciones.csv.
 * Si una institución ya está en el catálogo (mismo municipio y nombre parecido), no la duplica.
 * Las sedes (seccionales) se guardan como "Nombre - Sede Municipio" porque el nombre es único.
 * La ubicación de las nuevas es el centro del municipio (no la del campus): se marca como aproximada.
 */
@Component
@Order(3)
public class ImportadorUniversidades implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ImportadorUniversidades.class);
    private final UniversidadRepository universidades;
    private final GeografiaService geografia;

    public ImportadorUniversidades(UniversidadRepository universidades, GeografiaService geografia) {
        this.universidades = universidades;
        this.geografia = geografia;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<Universidad> existentes = new ArrayList<>(universidades.findAll());
        int nuevas = 0;
        try (var r = new BufferedReader(new InputStreamReader(new ClassPathResource("datos/instituciones.csv").getInputStream(), StandardCharsets.UTF_8))) {
            r.readLine();
            String linea;
            while ((linea = r.readLine()) != null) {
                String[] c = linea.split(";", -1);
                String nombre = c[0], sede = c[1];
                Optional<GeografiaService.Municipio> m = geografia.porCodigo(c[2]);
                if (m.isEmpty()) continue;
                String ciudad = m.get().nombre();
                boolean esSede = "Seccional".equalsIgnoreCase(sede);
                String nombreBd = esSede ? nombre + " - Sede " + ciudad : nombre;
                if (yaExiste(existentes, nombre, ciudad) || universidades.findByNombre(nombreBd).isPresent()) continue;
                Universidad u = new Universidad(nombreBd, null, ciudad);
                u.ubicarAproximada(m.get().lat(), m.get().lon());
                u.setWeb(c[6]);
                universidades.save(u);
                existentes.add(u);
                nuevas++;
            }
        } catch (Exception e) {
            log.warn("No se pudieron importar las universidades oficiales: {}", e.getMessage());
            return;
        }
        if (nuevas > 0) log.info("Universidades oficiales importadas: {}", nuevas);
    }

    /** Coincide si en el mismo municipio hay una universidad cuyo nombre empieza igual (p. ej. "Universidad del Norte" = "UNIVERSIDAD DEL NORTE"). */
    private boolean yaExiste(List<Universidad> lista, String nombreOficial, String ciudad) {
        String o = GeografiaService.clave(nombreOficial);
        for (Universidad u : lista) {
            if (!ciudad.equalsIgnoreCase(u.getCiudad())) continue;
            String n = GeografiaService.clave(u.getNombre().replaceAll(" - .*$", ""));
            if (n.equals(o) || o.startsWith(n) || n.startsWith(o)) return true;
        }
        return false;
    }
}
