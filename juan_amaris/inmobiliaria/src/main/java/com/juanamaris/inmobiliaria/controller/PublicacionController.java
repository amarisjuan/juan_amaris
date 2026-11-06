package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.model.Inmueble;
import com.juanamaris.inmobiliaria.model.Operacion;
import com.juanamaris.inmobiliaria.model.TipoInmueble;
import com.juanamaris.inmobiliaria.model.Usuario;
import com.juanamaris.inmobiliaria.service.InmuebleService;
import com.juanamaris.inmobiliaria.service.UniversidadService;
import com.juanamaris.inmobiliaria.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
public class PublicacionController {

    private final InmuebleService inmuebles;
    private final UsuarioService usuarios;
    private final UniversidadService universidades;
    private final com.juanamaris.inmobiliaria.service.CompaneroService companeros;

    public PublicacionController(InmuebleService inmuebles, UsuarioService usuarios, UniversidadService universidades,
                                 com.juanamaris.inmobiliaria.service.CompaneroService companeros) {
        this.companeros = companeros;
        this.universidades = universidades;
        this.inmuebles = inmuebles;
        this.usuarios = usuarios;
    }

    /** Estos campos los controla el sistema, nunca el formulario. */
    @InitBinder("inmueble")
    public void protegerCampos(WebDataBinder binder) {
        binder.setDisallowedFields("id", "propietario*", "fotos*", "activo", "fechaPublicacion");
    }

    @ModelAttribute
    public void listas(Model model) {
        model.addAttribute("tipos", TipoInmueble.values());
        model.addAttribute("operaciones", Operacion.values());
        model.addAttribute("convivencias", com.juanamaris.inmobiliaria.model.Convivencia.values());
        model.addAttribute("generos", com.juanamaris.inmobiliaria.model.Genero.values());
        model.addAttribute("universidadesPorCiudad", universidades.porCiudad());
        model.addAttribute("puntosUniversidades", universidades.puntos());
    }

    @GetMapping("/mis-inmuebles")
    public String misInmuebles(@AuthenticationPrincipal UserDetails yo, Model model) {
        Usuario u = usuario(yo);
        model.addAttribute("inmuebles", inmuebles.deUsuario(u));
        model.addAttribute("misCompaneros", companeros.deUsuario(u));
        return "inmuebles/mis_inmuebles";
    }

    @GetMapping("/publicar")
    public String nuevo(Model model) {
        model.addAttribute("inmueble", new Inmueble());
        return "inmuebles/form";
    }

    @PostMapping("/publicar")
    public String publicar(@Valid @ModelAttribute Inmueble inmueble, BindingResult errores,
                           @RequestParam(name = "nuevasFotos", required = false) List<MultipartFile> nuevasFotos,
                           @AuthenticationPrincipal UserDetails yo, Model model, RedirectAttributes flash) {
        if (errores.hasErrors()) return "inmuebles/form";
        try {
            Inmueble guardado = inmuebles.publicar(inmueble, nuevasFotos, usuario(yo));
            flash.addFlashAttribute("ok", "¡Tu inmueble ya está publicado!");
            return "redirect:/inmuebles/" + guardado.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "inmuebles/form";
        }
    }

    @GetMapping("/inmuebles/{id}/editar")
    public String editar(@PathVariable Long id, @AuthenticationPrincipal UserDetails yo, Model model) {
        model.addAttribute("inmueble", inmuebles.paraEditar(id, usuario(yo)));
        return "inmuebles/form";
    }

    @PostMapping("/inmuebles/{id}/editar")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute Inmueble inmueble, BindingResult errores,
                             @RequestParam(name = "nuevasFotos", required = false) List<MultipartFile> nuevasFotos,
                             @RequestParam(name = "borrarFotos", required = false) List<String> borrarFotos,
                             @AuthenticationPrincipal UserDetails yo, Model model, RedirectAttributes flash) {
        Usuario u = usuario(yo);
        if (errores.hasErrors()) {
            inmueble.setId(id);
            inmueble.setFotos(inmuebles.paraEditar(id, u).getFotos());
            return "inmuebles/form";
        }
        try {
            inmuebles.actualizar(id, inmueble, nuevasFotos, borrarFotos, u);
            flash.addFlashAttribute("ok", "Cambios guardados");
            return "redirect:/mis-inmuebles";
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/inmuebles/" + id + "/editar";
        }
    }

    @PostMapping("/inmuebles/{id}/estado")
    public String estado(@PathVariable Long id, @AuthenticationPrincipal UserDetails yo) {
        inmuebles.cambiarEstado(id, usuario(yo));
        return "redirect:/mis-inmuebles";
    }

    @PostMapping("/inmuebles/{id}/eliminar")
    public String eliminar(@PathVariable Long id, @AuthenticationPrincipal UserDetails yo, RedirectAttributes flash) {
        inmuebles.eliminar(id, usuario(yo));
        flash.addFlashAttribute("ok", "Inmueble eliminado");
        return "redirect:/mis-inmuebles";
    }

    private Usuario usuario(UserDetails yo) { return usuarios.porEmail(yo.getUsername()); }
}
