package com.juanamaris.inmobiliaria.config;

import com.juanamaris.inmobiliaria.model.Universidad;
import com.juanamaris.inmobiliaria.repository.UniversidadRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.List;

/** Carga las principales universidades de Colombia con su ubicación. Solo agrega o ubica las que falten. */
@Component
@Order(1)
public class CatalogoUniversidades implements CommandLineRunner {

    private static final List<String[]> UNIVERSIDADES = List.of(
            // nombre, sigla, ciudad, latitud, longitud (coordenadas aproximadas del campus principal)
            new String[]{"Universidad del Norte", "Uninorte", "Barranquilla", "11.019", "-74.8505"},
            new String[]{"Universidad del Atlántico", "Uniatlántico", "Barranquilla", "11.0186", "-74.8711"},
            new String[]{"Universidad de la Costa", "CUC", "Barranquilla", "10.9958", "-74.7907"},
            new String[]{"Universidad Simón Bolívar", "Unisimón", "Barranquilla", "10.9927", "-74.7915"},
            new String[]{"Universidad Autónoma del Caribe", "Uniautónoma", "Barranquilla", "11.0049", "-74.8135"},
            new String[]{"Universidad Libre - Barranquilla", "Unilibre Barranquilla", "Barranquilla", "10.9873", "-74.7887"},
            new String[]{"Universidad Nacional de Colombia - Bogotá", "UNAL Bogotá", "Bogotá", "4.6381", "-74.084"},
            new String[]{"Universidad de los Andes", "Uniandes", "Bogotá", "4.6015", "-74.0661"},
            new String[]{"Pontificia Universidad Javeriana - Bogotá", "Javeriana", "Bogotá", "4.6286", "-74.0646"},
            new String[]{"Universidad del Rosario", "Rosario", "Bogotá", "4.6007", "-74.0732"},
            new String[]{"Universidad Externado de Colombia", "Externado", "Bogotá", "4.5953", "-74.0684"},
            new String[]{"Universidad Distrital Francisco José de Caldas", "Distrital", "Bogotá", "4.6283", "-74.0654"},
            new String[]{"Universidad Jorge Tadeo Lozano", "Tadeo", "Bogotá", "4.6024", "-74.068"},
            new String[]{"Universidad de Antioquia", "UdeA", "Medellín", "6.2677", "-75.5689"},
            new String[]{"Universidad EAFIT", "EAFIT", "Medellín", "6.2003", "-75.5784"},
            new String[]{"Universidad Pontificia Bolivariana", "UPB", "Medellín", "6.2425", "-75.5895"},
            new String[]{"Universidad Nacional de Colombia - Medellín", "UNAL Medellín", "Medellín", "6.2615", "-75.5778"},
            new String[]{"Universidad de Medellín", "UdeM", "Medellín", "6.2311", "-75.6105"},
            new String[]{"Universidad del Valle", "Univalle", "Cali", "3.3753", "-76.5336"},
            new String[]{"Universidad Icesi", "Icesi", "Cali", "3.3417", "-76.53"},
            new String[]{"Pontificia Universidad Javeriana - Cali", "Javeriana Cali", "Cali", "3.3484", "-76.5317"},
            new String[]{"Universidad Autónoma de Occidente", "UAO", "Cali", "3.3534", "-76.5233"},
            new String[]{"Universidad de Cartagena", "UdeC", "Cartagena", "10.4241", "-75.5506"},
            new String[]{"Universidad Tecnológica de Bolívar", "UTB", "Cartagena", "10.3698", "-75.4664"},
            new String[]{"Universidad Industrial de Santander", "UIS", "Bucaramanga", "7.1395", "-73.1206"},
            new String[]{"Universidad Autónoma de Bucaramanga", "UNAB", "Bucaramanga", "7.1172", "-73.1056"},
            new String[]{"Universidad del Magdalena", "Unimagdalena", "Santa Marta", "11.2236", "-74.1866"},
            new String[]{"Universidad Tecnológica de Pereira", "UTP", "Pereira", "4.793", "-75.69"},
            new String[]{"Universidad de Caldas", "Ucaldas", "Manizales", "5.0563", "-75.4923"});

    private final UniversidadRepository repo;

    public CatalogoUniversidades(UniversidadRepository repo) { this.repo = repo; }

    @Override
    public void run(String... args) {
        var existentes = repo.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Universidad::getNombre, u -> u));
        for (String[] u : UNIVERSIDADES) {
            Universidad uni = existentes.getOrDefault(u[0], null);
            if (uni == null) uni = new Universidad(u[0], u[1], u[2]);
            if (!uni.isUbicada()) {
                uni.ubicar(Double.valueOf(u[3]), Double.valueOf(u[4]));
                repo.save(uni);
            }
        }
    }
}
