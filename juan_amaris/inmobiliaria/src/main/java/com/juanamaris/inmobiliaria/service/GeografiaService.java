package com.juanamaris.inmobiliaria.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;

/** Municipios de Colombia (DIVIPOLA, MinSalud). Fuente: src/main/resources/datos/municipios.csv. */
@Service
public class GeografiaService {

    public record Municipio(String codigo, String departamento, String nombre, String oficial, double lat, double lon) {}

    private final List<Municipio> municipios;
    private final Map<String, Municipio> porNombre = new HashMap<>();

    public GeografiaService() {
        List<Municipio> lista = new ArrayList<>();
        try (var r = new BufferedReader(new InputStreamReader(new ClassPathResource("datos/municipios.csv").getInputStream(), StandardCharsets.UTF_8))) {
            r.readLine(); // cabecera
            String linea;
            while ((linea = r.readLine()) != null) {
                String[] c = linea.split(";");
                lista.add(new Municipio(c[0], c[1], c[2], c[3], Double.parseDouble(c[4]), Double.parseDouble(c[5])));
            }
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer datos/municipios.csv", e);
        }
        this.municipios = List.copyOf(lista);
        for (Municipio m : municipios) {
            porNombre.putIfAbsent(clave(m.nombre()), m);
            porNombre.putIfAbsent(clave(m.oficial()), m);
        }
    }

    public List<Municipio> todos() { return municipios; }

    /** Municipios agrupados por departamento, para el menú desplegable. */
    public Map<String, List<Municipio>> porDepartamento() {
        Map<String, List<Municipio>> m = new TreeMap<>();
        for (Municipio x : municipios) m.computeIfAbsent(x.departamento(), k -> new ArrayList<>()).add(x);
        return m;
    }

    public Optional<Municipio> porCodigo(String codigo) {
        return municipios.stream().filter(m -> m.codigo().equals(codigo)).findFirst();
    }

    /** Devuelve el municipio por su nombre de uso común u oficial (sin tildes ni mayúsculas), o null. */
    public Optional<Municipio> buscar(String nombre) {
        return Optional.ofNullable(porNombre.get(clave(nombre)));
    }

    static String clave(String s) {
        String t = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return t.toUpperCase().replaceAll("[^A-Z0-9]+", " ").trim();
    }
}
