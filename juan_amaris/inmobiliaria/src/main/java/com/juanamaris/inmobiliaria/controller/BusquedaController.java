package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.model.FiltroBusqueda;
import com.juanamaris.inmobiliaria.model.Operacion;
import com.juanamaris.inmobiliaria.model.TipoInmueble;
import com.juanamaris.inmobiliaria.service.InmuebleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BusquedaController {

    private final InmuebleService service;

    public BusquedaController(InmuebleService service) { this.service = service; }

    @GetMapping("/")
    public String buscar(@ModelAttribute("filtro") FiltroBusqueda filtro,
                         @RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("resultados", service.buscar(filtro, pagina));
        model.addAttribute("ciudades", service.ciudades());
        model.addAttribute("tipos", TipoInmueble.values());
        model.addAttribute("operaciones", Operacion.values());
        return "inmuebles/buscar";
    }

    @GetMapping("/inmuebles/{id:\\d+}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("inmueble", service.detallePublico(id));
        return "inmuebles/detalle";
    }
}
