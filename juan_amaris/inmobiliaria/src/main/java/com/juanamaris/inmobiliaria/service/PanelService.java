package com.juanamaris.inmobiliaria.service;

import com.juanamaris.inmobiliaria.model.Inmueble;
import com.juanamaris.inmobiliaria.model.TipoEvento;
import com.juanamaris.inmobiliaria.repository.CompaneroRepository;
import com.juanamaris.inmobiliaria.repository.ConteoDiarioRepository;
import com.juanamaris.inmobiliaria.repository.InmuebleRepository;
import com.juanamaris.inmobiliaria.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

/** Estadísticas agregadas para el panel del administrador. */
@Service
@Transactional(readOnly = true)
public class PanelService {

    public static final Set<TipoEvento> CONTACTOS = EnumSet.of(TipoEvento.CONTACTO_WHATSAPP, TipoEvento.CONTACTO_LLAMADA);

    public record Periodo(String nombre, long visitas, long visitantes, long vistasInmuebles, long whatsapp, long llamadas,
                          long contactosCompaneros) {
        public long contactos() { return whatsapp + llamadas; }
    }
    public record Dia(LocalDate fecha, long valor) {}
    public record FilaInmueble(Long id, String titulo, String ciudad, boolean activo, long vistas, long contactos) {
        public String tasa() { return vistas == 0 ? "-" : Math.round(100.0 * contactos / vistas) + "%"; }
    }
    public record FilaBusqueda(String descripcion, long veces) {}

    private final ConteoDiarioRepository conteos;
    private final InmuebleRepository inmuebles;
    private final UsuarioRepository usuarios;
    private final CompaneroRepository companeros;
    private final Clock reloj;

    public PanelService(ConteoDiarioRepository conteos, InmuebleRepository inmuebles, UsuarioRepository usuarios,
                        CompaneroRepository companeros, Clock reloj) {
        this.conteos = conteos;
        this.inmuebles = inmuebles;
        this.usuarios = usuarios;
        this.companeros = companeros;
        this.reloj = reloj;
    }

    public LocalDate hoy() { return LocalDate.now(reloj); }

    public List<Periodo> periodos() {
        LocalDate hoy = hoy();
        return List.of(periodo("Hoy", hoy), periodo("Últimos 7 días", hoy.minusDays(6)), periodo("Últimos 30 días", hoy.minusDays(29)));
    }

    private Periodo periodo(String nombre, LocalDate desde) {
        return new Periodo(nombre, total(TipoEvento.VISITA_PAGINA, desde), total(TipoEvento.VISITANTE, desde),
                total(TipoEvento.VER_INMUEBLE, desde), total(TipoEvento.CONTACTO_WHATSAPP, desde),
                total(TipoEvento.CONTACTO_LLAMADA, desde), total(TipoEvento.CONTACTO_COMPANERO, desde));
    }

    private long total(TipoEvento t, LocalDate desde) { return conteos.total(EnumSet.of(t), desde); }

    /** Serie diaria completa (los días sin actividad van en 0). */
    public List<Dia> serie(Set<TipoEvento> tipos, int dias) {
        LocalDate desde = hoy().minusDays(dias - 1L);
        Map<LocalDate, Long> mapa = new HashMap<>();
        for (Object[] f : conteos.porDia(tipos, desde)) mapa.put((LocalDate) f[0], ((Number) f[1]).longValue());
        List<Dia> serie = new ArrayList<>();
        for (int k = 0; k < dias; k++) serie.add(new Dia(desde.plusDays(k), mapa.getOrDefault(desde.plusDays(k), 0L)));
        return serie;
    }

    public Map<String, Long> totales() {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("usuarios", usuarios.count());
        m.put("usuariosNuevos7", usuarios.countByFechaRegistroAfter(hoy().minusDays(6).atStartOfDay()));
        m.put("inmueblesActivos", inmuebles.countByActivoTrue());
        m.put("inmuebles", inmuebles.count());
        m.put("companerosActivos", companeros.countByActivoTrue());
        return m;
    }

    /** Inmuebles con vistas o contactos en los últimos 30 días, ordenados por contactos y luego vistas. */
    public List<FilaInmueble> rendimientoInmuebles() {
        LocalDate desde = hoy().minusDays(29);
        Map<Long, long[]> datos = new HashMap<>(); // [vistas, contactos]
        for (Object[] f : conteos.porClave(EnumSet.of(TipoEvento.VER_INMUEBLE), desde))
            datos.computeIfAbsent(Long.valueOf((String) f[0]), k -> new long[2])[0] = ((Number) f[1]).longValue();
        for (Object[] f : conteos.porClave(CONTACTOS, desde))
            datos.computeIfAbsent(Long.valueOf((String) f[0]), k -> new long[2])[1] = ((Number) f[1]).longValue();
        Map<Long, Inmueble> porId = new HashMap<>();
        inmuebles.findAllById(datos.keySet()).forEach(i -> porId.put(i.getId(), i));
        return datos.entrySet().stream().filter(e -> porId.containsKey(e.getKey()))
                .map(e -> {
                    Inmueble i = porId.get(e.getKey());
                    return new FilaInmueble(i.getId(), i.getTitulo(), i.getCiudad(), i.isActivo(), e.getValue()[0], e.getValue()[1]);
                })
                .sorted(Comparator.comparingLong(FilaInmueble::contactos).reversed()
                        .thenComparing(Comparator.comparingLong(FilaInmueble::vistas).reversed()))
                .toList();
    }

    public List<FilaBusqueda> busquedas(TipoEvento tipo, int limite) {
        return conteos.porClave(EnumSet.of(tipo), hoy().minusDays(29)).stream().limit(limite)
                .map(f -> new FilaBusqueda((String) f[0], ((Number) f[1]).longValue())).toList();
    }
}
