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
    private final com.juanamaris.inmobiliaria.repository.CompaneroRepository companeros;

    public DatosDemo(UsuarioRepository usuarios, InmuebleRepository inmuebles, PasswordEncoder encoder,
                     UniversidadRepository universidades,
                     com.juanamaris.inmobiliaria.repository.CompaneroRepository companeros) {
        this.companeros = companeros;
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

        ubicar(crear(admin, "Apartamento con vista al mar", TipoInmueble.APARTAMENTO, Operacion.ARRIENDO,
                2_800_000L, "Barranquilla", "Alto Prado", 5, 85, 3, 2, 1), 11.0040, -74.8070);
        ubicar(crear(admin, "Casa familiar con patio", TipoInmueble.CASA, Operacion.VENTA,
                420_000_000L, "Barranquilla", "Villa Country", 4, 160, 4, 3, 2), 10.9990, -74.8090);
        estudiantil(crear(admin, "Habitación amoblada a pasos de Uninorte", TipoInmueble.HABITACION, Operacion.ARRIENDO,
                900_000L, "Barranquilla", "Villa Santos", 5, 16, 1, 1, 0), "Uninorte", 8, true, true, 11.0150, -74.8460);
        estudiantil(crear(admin, "Apartaestudio para estudiantes cerca a la CUC", TipoInmueble.APARTAESTUDIO, Operacion.ARRIENDO,
                1_100_000L, "Barranquilla", "El Prado", 4, 30, 1, 1, 0), "CUC", 12, true, false, 10.9998, -74.7980);
        Inmueble aptoEstudio = crear(admin, "Apartaestudio amoblado cerca a universidades", TipoInmueble.APARTAESTUDIO, Operacion.ARRIENDO,
                1_300_000L, "Medellín", "Laureles", 4, 35, 1, 1, 0);
        estudiantil(aptoEstudio, "UPB", 10, true, false, 6.2450, -75.5960);
        ubicar(crear(admin, "Apartamento nuevo para estrenar", TipoInmueble.APARTAMENTO, Operacion.VENTA,
                380_000_000L, "Bogotá", "Cedritos", 4, 68, 2, 2, 1), 4.7240, -74.0390);
        ubicar(crear(admin, "Local comercial sobre avenida principal", TipoInmueble.LOCAL, Operacion.ARRIENDO,
                4_500_000L, "Cartagena", "Bocagrande", 6, 60, 0, 1, 0), 10.3990, -75.5560);
        Inmueble habBogota = crear(admin, "Habitación para estudiante con servicios incluidos", TipoInmueble.HABITACION, Operacion.ARRIENDO,
                750_000L, "Bogotá", "Teusaquillo", 3, 14, 1, 1, 0);
        estudiantil(habBogota, "UNAL Bogotá", 15, true, true, 4.6330, -74.0760);
        habBogota.setConvivencia(Convivencia.SOLO_MUJERES);
        habBogota.setHorarioLlegada("Hasta las 11:00 p. m.");
        habBogota.setOtrasReglas("Cocina compartida. Se hace aseo por turnos.");
        inmuebles.save(habBogota);
        aptoEstudio.setPermiteMascotas(true);
        inmuebles.save(aptoEstudio);

        Usuario laura = new Usuario();
        laura.setNombre("Laura (ejemplo)");
        laura.setEmail("laura@ejemplo.co");
        laura.setCelular("3007654321");
        laura.setPassword(encoder.encode("laura12345"));
        usuarios.save(laura);
        companero(laura, TipoBusquedaCompanero.BUSCO_CUARTO, "Uninorte", "Ingeniería Industrial", 3, Genero.MUJER,
                Genero.MUJER, 900_000L, "Villa Santos", "Valledupar",
                "Soy tranquila y ordenada. Busco compartir apartamento con otra estudiante cerca a Uninorte.");
        companero(admin, TipoBusquedaCompanero.TENGO_CUARTO, "UNAL Bogotá", "Economía", 6, Genero.HOMBRE,
                null, 700_000L, "Teusaquillo", "Ibagué",
                "Tengo una habitación libre en mi apartamento a 10 minutos de la Nacional. Servicios incluidos.");
    }

    private void companero(Usuario autor, TipoBusquedaCompanero tipo, String sigla, String carrera, int semestre,
                           Genero genero, Genero busco, long presupuesto, String barrio, String origen, String texto) {
        universidades.findBySigla(sigla).ifPresent(u -> {
            Companero c = new Companero();
            c.setAutor(autor);
            c.setTipo(tipo);
            c.setUniversidad(u);
            c.setCarrera(carrera);
            c.setSemestre(semestre);
            c.setGenero(genero);
            c.setBuscoGenero(busco);
            c.setPresupuesto(presupuesto);
            c.setBarrio(barrio);
            c.setCiudadOrigen(origen);
            c.setDescripcion(texto);
            companeros.save(c);
        });
    }

    private void ubicar(Inmueble i, double lat, double lng) {
        i.setLatitud(lat);
        i.setLongitud(lng);
        inmuebles.save(i);
    }

    private void estudiantil(Inmueble i, String sigla, int minutos, boolean amoblado, boolean servicios,
                             double lat, double lng) {
        i.setLatitud(lat);
        i.setLongitud(lng);
        inmuebles.save(i);
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
