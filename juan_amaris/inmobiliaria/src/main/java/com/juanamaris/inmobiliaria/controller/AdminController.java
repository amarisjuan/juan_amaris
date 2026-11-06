package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.model.TipoEvento;
import com.juanamaris.inmobiliaria.service.PanelService;
import com.juanamaris.inmobiliaria.service.RegistroAnuncianteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.EnumSet;

/** Panel del administrador. Toda la ruta /admin/** exige rol ADMIN (SeguridadConfig). */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PanelService panel;
    private final RegistroAnuncianteService registros;

    public AdminController(PanelService panel, RegistroAnuncianteService registros) {
        this.panel = panel;
        this.registros = registros;
    }

    @GetMapping
    public String inicio(Model model) {
        var inmuebles = panel.rendimientoInmuebles();
        model.addAttribute("periodos", panel.periodos());
        model.addAttribute("totales", panel.totales());
        model.addAttribute("serieVisitas", paraGrafica(panel.serie(EnumSet.of(TipoEvento.VISITA_PAGINA), 30)));
        model.addAttribute("serieContactos", paraGrafica(panel.serie(PanelService.CONTACTOS, 30)));
        model.addAttribute("masContactados", inmuebles.stream().filter(f -> f.contactos() > 0).limit(10).toList());
        model.addAttribute("sinContacto", inmuebles.stream().filter(f -> f.contactos() == 0 && f.vistas() >= 3).limit(10).toList());
        model.addAttribute("sinResultados", panel.busquedas(TipoEvento.BUSQUEDA_SIN_RESULTADOS, 15));
        model.addAttribute("masBuscado", panel.busquedas(TipoEvento.BUSQUEDA, 10));
        model.addAttribute("hoy", panel.hoy());
        return "admin/panel";
    }

    private static final java.time.format.DateTimeFormatter CORTA =
            java.time.format.DateTimeFormatter.ofPattern("d MMM", new java.util.Locale("es", "CO"));

    /** Fechas ya formateadas para el JavaScript (evita depender de cómo se serializan las fechas). */
    private static java.util.List<java.util.Map<String, Object>> paraGrafica(java.util.List<PanelService.Dia> serie) {
        return serie.stream().map(d -> java.util.Map.<String, Object>of(
                "etiqueta", d.fecha().format(CORTA).replace(".", ""), "valor", d.valor())).toList();
    }

    @GetMapping("/anunciantes")
    public String anunciantes(Model model) {
        model.addAttribute("registros", registros.todos());
        return "admin/anunciantes";
    }
}
