package com.juanamaris.inmobiliaria.config;

import com.juanamaris.inmobiliaria.model.Universidad;
import com.juanamaris.inmobiliaria.repository.UniversidadRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.List;

/** Carga las principales universidades de Colombia. Solo agrega las que falten, así se puede ampliar la lista. */
@Component
@Order(1)
public class CatalogoUniversidades implements CommandLineRunner {

    private static final List<String[]> UNIVERSIDADES = List.of(
            // nombre, sigla, ciudad
            new String[]{"Universidad del Norte", "Uninorte", "Barranquilla"},
            new String[]{"Universidad del Atlántico", "Uniatlántico", "Barranquilla"},
            new String[]{"Universidad de la Costa", "CUC", "Barranquilla"},
            new String[]{"Universidad Simón Bolívar", "Unisimón", "Barranquilla"},
            new String[]{"Universidad Autónoma del Caribe", "Uniautónoma", "Barranquilla"},
            new String[]{"Universidad Libre - Barranquilla", "Unilibre Barranquilla", "Barranquilla"},
            new String[]{"Universidad Nacional de Colombia - Bogotá", "UNAL Bogotá", "Bogotá"},
            new String[]{"Universidad de los Andes", "Uniandes", "Bogotá"},
            new String[]{"Pontificia Universidad Javeriana - Bogotá", "Javeriana", "Bogotá"},
            new String[]{"Universidad del Rosario", "Rosario", "Bogotá"},
            new String[]{"Universidad Externado de Colombia", "Externado", "Bogotá"},
            new String[]{"Universidad Distrital Francisco José de Caldas", "Distrital", "Bogotá"},
            new String[]{"Universidad Jorge Tadeo Lozano", "Tadeo", "Bogotá"},
            new String[]{"Universidad de Antioquia", "UdeA", "Medellín"},
            new String[]{"Universidad EAFIT", "EAFIT", "Medellín"},
            new String[]{"Universidad Pontificia Bolivariana", "UPB", "Medellín"},
            new String[]{"Universidad Nacional de Colombia - Medellín", "UNAL Medellín", "Medellín"},
            new String[]{"Universidad de Medellín", "UdeM", "Medellín"},
            new String[]{"Universidad del Valle", "Univalle", "Cali"},
            new String[]{"Universidad Icesi", "Icesi", "Cali"},
            new String[]{"Pontificia Universidad Javeriana - Cali", "Javeriana Cali", "Cali"},
            new String[]{"Universidad Autónoma de Occidente", "UAO", "Cali"},
            new String[]{"Universidad de Cartagena", "UdeC", "Cartagena"},
            new String[]{"Universidad Tecnológica de Bolívar", "UTB", "Cartagena"},
            new String[]{"Universidad Industrial de Santander", "UIS", "Bucaramanga"},
            new String[]{"Universidad Autónoma de Bucaramanga", "UNAB", "Bucaramanga"},
            new String[]{"Universidad del Magdalena", "Unimagdalena", "Santa Marta"},
            new String[]{"Universidad Tecnológica de Pereira", "UTP", "Pereira"},
            new String[]{"Universidad de Caldas", "Ucaldas", "Manizales"});

    private final UniversidadRepository repo;

    public CatalogoUniversidades(UniversidadRepository repo) { this.repo = repo; }

    @Override
    public void run(String... args) {
        var existentes = repo.findAll().stream().map(Universidad::getNombre).toList();
        for (String[] u : UNIVERSIDADES) {
            if (!existentes.contains(u[0])) repo.save(new Universidad(u[0], u[1], u[2]));
        }
    }
}
