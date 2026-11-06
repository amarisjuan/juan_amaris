package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.model.*;
import com.juanamaris.inmobiliaria.service.CompaneroService;
import com.juanamaris.inmobiliaria.service.UniversidadService;
import com.juanamaris.inmobiliaria.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping("/companeros")
public class CompaneroController {

    private final CompaneroService companeros;
    private final UniversidadService universidades;
    private final UsuarioService usuarios;

    private final com.juanamaris.inmobiliaria.service.GeografiaService geografia;

    public CompaneroController(CompaneroService companeros, UniversidadService universidades, UsuarioService usuarios,
                               com.juanamaris.inmobiliaria.service.GeografiaService geografia) {
        this.geografia = geografia;
        this.companeros = companeros;
        this.universidades = universidades;
        this.usuarios = usuarios;
    }

    @InitBinder("companero")
    public void protegerCampos(WebDataBinder binder) {
        binder.setDisallowedFields("id", "autor*", "activo", "fechaPublicacion");
    }

    @ModelAttribute
    public void listas(Model model) {
        model.addAttribute("universidadesPorCiudad", universidades.porCiudad());
        model.addAttribute("municipios", geografia.porDepartamento());
        model.addAttribute("universidadesGeo", universidades.mapaPorCiudad());
        model.addAttribute("tiposBusqueda", TipoBusquedaCompanero.values());
        model.addAttribute("generos", Genero.values());
    }

    @GetMapping
    public String lista(@ModelAttribute("filtro") FiltroCompanero filtro, @RequestParam(defaultValue = "0") int pagina,
                        Model model, HttpServletRequest req) {
        model.addAttribute("resultados", companeros.buscar(filtro, pagina));
        model.addAttribute("universidadesFiltro", universidades.de(filtro.getCiudad()));
        String query = ServletUriComponentsBuilder.fromRequest(req).replaceQueryParam("pagina").build().getQuery();
        model.addAttribute("urlPaginas", "/companeros?" + (query == null ? "" : query + "&") + "pagina=");
        return "companeros/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("companero", new Companero());
        return "companeros/form";
    }

    @PostMapping("/nuevo")
    public String publicar(@Valid @ModelAttribute Companero companero, BindingResult errores,
                           @RequestParam(required = false) String ciudad,
                           @AuthenticationPrincipal UserDetails yo, Model model, RedirectAttributes flash) {
        if (companero.getUniversidad() != null && (ciudad == null || ciudad.isBlank())) {
            errores.rejectValue("universidad", "ciudad", "Primero elige la ciudad y luego la universidad.");
        } else if (companero.getUniversidad() != null && !companero.getUniversidad().getCiudad().equalsIgnoreCase(ciudad.trim())) {
            errores.rejectValue("universidad", "ciudad", "Esa universidad no está en la ciudad que elegiste.");
        }
        for (String campo : java.util.List.of("barrio", "descripcion")) {
            String texto = "barrio".equals(campo) ? companero.getBarrio() : companero.getDescripcion();
            com.juanamaris.inmobiliaria.service.DetectorDirecciones.buscar(texto).ifPresent(f -> errores.rejectValue(
                    campo, "direccion", com.juanamaris.inmobiliaria.service.DetectorDirecciones.mensaje(f)));
        }
        if (errores.hasErrors()) return "companeros/form";
        try {
            companeros.publicar(companero, usuario(yo));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "companeros/form";
        }
        flash.addFlashAttribute("ok", "¡Listo! Tu publicación ya aparece en el tablero.");
        return "redirect:/companeros";
    }

    @PostMapping("/{id}/estado")
    public String estado(@PathVariable Long id, @AuthenticationPrincipal UserDetails yo) {
        companeros.cambiarEstado(id, usuario(yo));
        return "redirect:/mis-inmuebles";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, @AuthenticationPrincipal UserDetails yo, RedirectAttributes flash) {
        companeros.eliminar(id, usuario(yo));
        flash.addFlashAttribute("ok", "Publicación eliminada");
        return "redirect:/mis-inmuebles";
    }

    private Usuario usuario(UserDetails yo) { return usuarios.porEmail(yo.getUsername()); }
}
