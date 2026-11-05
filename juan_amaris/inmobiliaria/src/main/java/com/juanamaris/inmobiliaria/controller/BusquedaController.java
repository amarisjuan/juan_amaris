package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.model.FiltroBusqueda;
import com.juanamaris.inmobiliaria.model.Operacion;
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
        model.addAttribute("resultados", service.buscar(filtro, pagina));
        model.addAttribute("ciudades", service.ciudades());
        model.addAttribute("tipos", TipoInmueble.values());
        model.addAttribute("operaciones", Operacion.values());
        model.addAttribute("universidadesPorCiudad", universidades.porCiudad());
        if (filtro.getUniversidad() != null) {
            model.addAttribute("universidadElegida", universidades.buscar(filtro.getUniversidad()));
        }
        return "inmuebles/buscar";
    }

    @GetMapping("/estudiantes")
    public String estudiantes(Model model) {
        model.addAttribute("universidadesPorCiudad", universidades.porCiudad());
        model.addAttribute("conteos", universidades.inmueblesCercanos());
        return "inmuebles/estudiantes";
    }

    @GetMapping("/inmuebles/{id:\\d+}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("inmueble", service.detallePublico(id));
        return "inmuebles/detalle";
    }
}
