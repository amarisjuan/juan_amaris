package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.model.FiltroBusqueda;
import com.juanamaris.inmobiliaria.model.Inmueble;
import com.juanamaris.inmobiliaria.model.Operacion;
import com.juanamaris.inmobiliaria.model.PuntoMapa;
import com.juanamaris.inmobiliaria.model.Universidad;
import com.juanamaris.inmobiliaria.model.TipoInmueble;
import com.juanamaris.inmobiliaria.service.InmuebleService;
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

    public BusquedaController(InmuebleService service, UniversidadService universidades) {
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
                resultados.getContent().stream().filter(Inmueble::isUbicado).map(PuntoMapa::de).toList());
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
        return "inmuebles/buscar";
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
        model.addAttribute("inmueble", i);
        model.addAttribute("puntoInmueble", i.isUbicado() ? PuntoMapa.de(i) : null);
        Universidad u = i.getUniversidadCercana();
        model.addAttribute("puntoUniversidad", u != null && u.isUbicada() ? PuntoMapa.de(u) : null);
        return "inmuebles/detalle";
    }
}
