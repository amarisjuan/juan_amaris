package com.juanamaris.inmobiliaria.config;

import com.juanamaris.inmobiliaria.model.*;
import com.juanamaris.inmobiliaria.repository.InmuebleRepository;
import com.juanamaris.inmobiliaria.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.juanamaris.inmobiliaria.repository.UniversidadRepository;

/** Crea un usuario y algunos inmuebles de ejemplo la primera vez que se arranca. */
@Component
@Order(2)
@ConditionalOnProperty(name = "app.datos-demo", havingValue = "true")
public class DatosDemo implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final InmuebleRepository inmuebles;
    private final PasswordEncoder encoder;
    private final UniversidadRepository universidades;

    public DatosDemo(UsuarioRepository usuarios, InmuebleRepository inmuebles, PasswordEncoder encoder,
                     UniversidadRepository universidades) {
        this.universidades = universidades;
        this.usuarios = usuarios;
        this.inmuebles = inmuebles;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() > 0) return;

        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setEmail("admin@inmobiliaria.co");
        admin.setCelular("3001234567");
        admin.setPassword(encoder.encode("admin123"));
        admin.setRol("ADMIN");
        usuarios.save(admin);

        crear(admin, "Apartamento con vista al mar", TipoInmueble.APARTAMENTO, Operacion.ARRIENDO,
                2_800_000L, "Barranquilla", "Alto Prado", 5, 85, 3, 2, 1);
        crear(admin, "Casa familiar con patio", TipoInmueble.CASA, Operacion.VENTA,
                420_000_000L, "Barranquilla", "Villa Country", 4, 160, 4, 3, 2);
        estudiantil(crear(admin, "Habitación amoblada a pasos de Uninorte", TipoInmueble.HABITACION, Operacion.ARRIENDO,
                900_000L, "Barranquilla", "Villa Santos", 5, 16, 1, 1, 0), "Uninorte", 8, true, true);
        estudiantil(crear(admin, "Apartaestudio para estudiantes cerca a la CUC", TipoInmueble.APARTAESTUDIO, Operacion.ARRIENDO,
                1_100_000L, "Barranquilla", "El Prado", 4, 30, 1, 1, 0), "CUC", 12, true, false);
        Inmueble aptoEstudio = crear(admin, "Apartaestudio amoblado cerca a universidades", TipoInmueble.APARTAESTUDIO, Operacion.ARRIENDO,
                1_300_000L, "Medellín", "Laureles", 4, 35, 1, 1, 0);
        estudiantil(aptoEstudio, "UPB", 10, true, false);
        crear(admin, "Apartamento nuevo para estrenar", TipoInmueble.APARTAMENTO, Operacion.VENTA,
                380_000_000L, "Bogotá", "Cedritos", 4, 68, 2, 2, 1);
        crear(admin, "Local comercial sobre avenida principal", TipoInmueble.LOCAL, Operacion.ARRIENDO,
                4_500_000L, "Cartagena", "Bocagrande", 6, 60, 0, 1, 0);
        Inmueble habBogota = crear(admin, "Habitación para estudiante con servicios incluidos", TipoInmueble.HABITACION, Operacion.ARRIENDO,
                750_000L, "Bogotá", "Teusaquillo", 3, 14, 1, 1, 0);
        estudiantil(habBogota, "UNAL Bogotá", 15, true, true);
    }

    private void estudiantil(Inmueble i, String sigla, int minutos, boolean amoblado, boolean servicios) {
        universidades.findBySigla(sigla).ifPresent(u -> {
            i.setUniversidadCercana(u);
            i.setMinutosAPie(minutos);
            i.setAmoblado(amoblado);
            i.setServiciosIncluidos(servicios);
            inmuebles.save(i);
        });
    }

    private Inmueble crear(Usuario dueno, String titulo, TipoInmueble tipo, Operacion op, long precio, String ciudad,
                       String barrio, int estrato, int area, int hab, int banos, int parq) {
        Inmueble i = new Inmueble();
        i.setTitulo(titulo);
        i.setDescripcion(titulo + " en el barrio " + barrio + ", " + ciudad
                + ". Inmueble de ejemplo: edítalo o elimínalo desde 'Mis inmuebles'.");
        i.setTipo(tipo);
        i.setOperacion(op);
        i.setPrecio(precio);
        i.setCiudad(ciudad);
        i.setBarrio(barrio);
        i.setEstrato(estrato);
        i.setAreaM2(area);
        i.setHabitaciones(hab);
        i.setBanos(banos);
        i.setParqueaderos(parq);
        i.setPropietario(dueno);
        return inmuebles.save(i);
    }
}
