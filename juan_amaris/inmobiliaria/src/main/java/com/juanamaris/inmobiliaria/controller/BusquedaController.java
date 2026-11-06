package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.model.FiltroBusqueda;
import com.juanamaris.inmobiliaria.model.Inmueble;
import com.juanamaris.inmobiliaria.model.Operacion;
import com.juanamaris.inmobiliaria.model.PuntoMapa;
import com.juanamaris.inmobiliaria.model.Universidad;
import com.juanamaris.inmobiliaria.model.TipoInmueble;
import com.juanamaris.inmobiliaria.service.InmuebleService;
import com.juanamaris.inmobiliaria.service.ActividadService;
import com.juanamaris.inmobiliaria.model.TipoEvento;
import com.juanamaris.inmobiliaria.service.UbicacionPrivadaService;
import com.juanamaris.inmobiliaria.service.UniversidadService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BusquedaController {

    private final InmuebleService service;
    private final UniversidadService universidades;
    private final UbicacionPrivadaService ubicacion;
    private final ActividadService actividad;

    public BusquedaController(InmuebleService service, UniversidadService universidades, UbicacionPrivadaService ubicacion,
                              ActividadService actividad) {
        this.ubicacion = ubicacion;
        this.actividad = actividad;
        this.service = service;
        this.universidades = universidades;
    }

    @GetMapping("/")
    public String buscar(@ModelAttribute("filtro") FiltroBusqueda filtro,
                         @RequestParam(defaultValue = "0") int pagina, Model model, HttpServletRequest req) {
        String query = ServletUriComponentsBuilder.fromRequest(req).replaceQueryParam("pagina").build().getQuery();
        model.addAttribute("urlPaginas", "/?" + (query == null ? "" : query + "&") + "pagina=");
        var resultados = service.buscar(filtro, pagina);
        model.addAttribute("resultados", resultados);
        model.addAttribute("puntosInmuebles",
                resultados.getContent().stream().filter(Inmueble::isZonaPublicaLista)
                        .map(i -> PuntoMapa.zona(i, ubicacion.getRadio())).toList());
        model.addAttribute("ciudades", service.ciudades());
        model.addAttribute("tipos", TipoInmueble.values());
        model.addAttribute("operaciones", Operacion.values());
        model.addAttribute("convivencias", com.juanamaris.inmobiliaria.model.Convivencia.values());
        model.addAttribute("generos", com.juanamaris.inmobiliaria.model.Genero.values());
        model.addAttribute("universidadesPorCiudad", universidades.porCiudad());
        if (filtro.getUniversidad() != null) {
            Universidad u = universidades.buscar(filtro.getUniversidad());
            model.addAttribute("universidadElegida", u);
            if (u != null && u.isUbicada()) model.addAttribute("puntoUniversidad", PuntoMapa.de(u));
        }
        if (pagina == 0) {
            String descripcion = describir(filtro, (Universidad) model.getAttribute("universidadElegida"));
            if (!descripcion.isEmpty()) {
                actividad.registrar(TipoEvento.BUSQUEDA, descripcion);
                if (resultados.getTotalElements() == 0) actividad.registrar(TipoEvento.BUSQUEDA_SIN_RESULTADOS, descripcion);
            }
        }
        return "inmuebles/buscar";
    }

    /** Descripción legible de lo que se buscó (sin datos de quién buscó). Vacía si no hay filtros. */
    static String describir(FiltroBusqueda f, Universidad u) {
        java.util.List<String> partes = new java.util.ArrayList<>();
        if (f.getOperacion() != null) partes.add(f.getOperacion().getEtiqueta());
        if (f.getTipo() != null) partes.add(f.getTipo().getEtiqueta());
        if (f.getCiudad() != null && !f.getCiudad().isBlank()) partes.add(capitalizar(f.getCiudad().trim()));
        if (u != null) partes.add("cerca a " + u.getNombreCorto());
        if (f.getTexto() != null && !f.getTexto().isBlank()) partes.add("\"" + f.getTexto().trim().toLowerCase() + "\"");
        if (f.getPrecioMax() != null) partes.add("hasta $" + String.format(java.util.Locale.GERMANY, "%,d", f.getPrecioMax()));
        if (f.getHabitacionesMin() != null) partes.add(f.getHabitacionesMin() + "+ hab.");
        if (f.getPara() != null) partes.add("para " + f.getPara().getEtiqueta().toLowerCase());
        if (Boolean.TRUE.equals(f.getAmoblado())) partes.add("amoblado");
        if (Boolean.TRUE.equals(f.getMascotas())) partes.add("con mascotas");
        return String.join(" · ", partes);
    }

    private static String capitalizar(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    @GetMapping("/estudiantes")
    public String estudiantes(Model model) {
        model.addAttribute("universidadesPorCiudad", universidades.porCiudad());
        model.addAttribute("conteos", universidades.inmueblesCercanos());
        model.addAttribute("puntosUniversidades", universidades.puntos());
        return "inmuebles/estudiantes";
    }

    @GetMapping("/inmuebles/{id:\\d+}")
    public String detalle(@PathVariable Long id, Model model) {
        Inmueble i = service.detallePublico(id);
        actividad.registrar(TipoEvento.VER_INMUEBLE, String.valueOf(id));
        model.addAttribute("inmueble", i);
        model.addAttribute("puntoInmueble", i.isZonaPublicaLista() ? PuntoMapa.zona(i, ubicacion.getRadio()) : null);
        Universidad u = i.getUniversidadCercana();
        model.addAttribute("puntoUniversidad", u != null && u.isUbicada() ? PuntoMapa.de(u) : null);
        model.addAttribute("distanciaUniversidad", ubicacion.distanciaAproximada(i, u));
        return "inmuebles/detalle";
    }
}
