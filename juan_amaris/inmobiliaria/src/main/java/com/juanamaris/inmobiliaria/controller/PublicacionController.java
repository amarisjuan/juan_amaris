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
    private final com.juanamaris.inmobiliaria.service.RegistroAnuncianteService registros;
    private final com.juanamaris.inmobiliaria.service.GeografiaService geografia;
    private final com.juanamaris.inmobiliaria.service.BarrioService barrios;

    public PublicacionController(InmuebleService inmuebles, UsuarioService usuarios, UniversidadService universidades,
                                 com.juanamaris.inmobiliaria.service.CompaneroService companeros,
                                 com.juanamaris.inmobiliaria.service.RegistroAnuncianteService registros,
                                 com.juanamaris.inmobiliaria.service.GeografiaService geografia,
                                 com.juanamaris.inmobiliaria.service.BarrioService barrios) {
        this.geografia = geografia;
        this.barrios = barrios;
        this.registros = registros;
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
        model.addAttribute("municipios", geografia.porDepartamento());
        model.addAttribute("universidadesGeo", universidades.mapaPorCiudad());
        model.addAttribute("barriosGeo", barrios.mapaPorCiudad());
    }

    /** La ciudad debe ser un municipio de Colombia y la universidad cercana debe estar en esa misma ciudad. */
    private void validarUbicacion(Inmueble i, BindingResult errores) {
        var m = geografia.buscar(i.getCiudad());
        if (m.isEmpty()) {
            errores.rejectValue("ciudad", "ciudad", "Elige la ciudad de la lista");
        } else {
            i.setCiudad(m.get().nombre());
        }
        var u = i.getUniversidadCercana();
        if (u != null && m.isPresent() && !u.getCiudad().equalsIgnoreCase(m.get().nombre())) {
            errores.rejectValue("universidadCercana", "ciudad",
                    "Esa universidad está en " + u.getCiudad() + ". Elige una universidad de " + m.get().nombre() + ".");
        }
    }

    @GetMapping("/mis-inmuebles")
    public String misInmuebles(@AuthenticationPrincipal UserDetails yo, Model model) {
        Usuario u = usuario(yo);
        model.addAttribute("inmuebles", inmuebles.deUsuario(u));
        model.addAttribute("misCompaneros", companeros.deUsuario(u));
        return "inmuebles/mis_inmuebles";
    }

    /** Ley 1480 de 2011, art. 53: antes de publicar, el anunciante debe dejar sus datos de identificación (privados). */
    private boolean faltaRegistro(UserDetails yo, RedirectAttributes flash) {
        if (registros.tieneRegistro(usuario(yo))) return false;
        flash.addFlashAttribute("error", "Antes de publicar necesitamos tus datos de anunciante. Son privados: no se publican.");
        return true;
    }

    @GetMapping("/publicar")
    public String nuevo(@AuthenticationPrincipal UserDetails yo, Model model, RedirectAttributes flash) {
        if (faltaRegistro(yo, flash)) return "redirect:/mi-registro";
        model.addAttribute("inmueble", new Inmueble());
        return "inmuebles/form";
    }

    @PostMapping("/publicar")
    public String publicar(@Valid @ModelAttribute Inmueble inmueble, BindingResult errores,
                           @RequestParam(name = "nuevasFotos", required = false) List<MultipartFile> nuevasFotos,
                           @AuthenticationPrincipal UserDetails yo, Model model, RedirectAttributes flash) {
        if (faltaRegistro(yo, flash)) return "redirect:/mi-registro";
        validarSinDireccion(inmueble, errores);
        validarUbicacion(inmueble, errores);
        if (errores.hasErrors()) return "inmuebles/form";
        try {
            Inmueble guardado = inmuebles.publicar(inmueble, nuevasFotos, usuario(yo));
            barrios.registrar(guardado.getCiudad(), guardado.getBarrio());
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
        validarSinDireccion(inmueble, errores);
        validarUbicacion(inmueble, errores);
        if (errores.hasErrors()) {
            inmueble.setId(id);
            inmueble.setFotos(inmuebles.paraEditar(id, u).getFotos());
            return "inmuebles/form";
        }
        try {
            inmuebles.actualizar(id, inmueble, nuevasFotos, borrarFotos, u);
            barrios.registrar(inmueble.getCiudad(), inmueble.getBarrio());
            flash.addFlashAttribute("ok", "Cambios guardados");
            return "redirect:/mis-inmuebles";
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/inmuebles/" + id + "/editar";
        }
    }

    /** El anunciante responde a la revisión y puede aportar pruebas (no sabe quién reportó). */
    @PostMapping("/inmuebles/{id}/respuesta-revision")
    public String responderRevision(@PathVariable Long id, @RequestParam String respuesta,
                                    @AuthenticationPrincipal UserDetails yo, RedirectAttributes flash) {
        if (respuesta.isBlank() || respuesta.length() > 2000) {
            flash.addFlashAttribute("error", "Escribe tu respuesta (máximo 2000 caracteres).");
        } else {
            inmuebles.responderRevision(id, respuesta.trim(), usuario(yo));
            flash.addFlashAttribute("ok", "Recibimos tu respuesta. Una persona la revisará.");
        }
        return "redirect:/mis-inmuebles";
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

    /** Rechaza direcciones exactas en los textos públicos del anuncio. */
    private void validarSinDireccion(Inmueble i, BindingResult errores) {
        java.util.Map<String, String> campos = new java.util.LinkedHashMap<>();
        campos.put("titulo", i.getTitulo());
        campos.put("barrio", i.getBarrio());
        campos.put("descripcion", i.getDescripcion());
        campos.put("otrasReglas", i.getOtrasReglas());
        campos.forEach((campo, texto) -> com.juanamaris.inmobiliaria.service.DetectorDirecciones.buscar(texto)
                .ifPresent(f -> errores.rejectValue(campo, "direccion",
                        com.juanamaris.inmobiliaria.service.DetectorDirecciones.mensaje(f))));
    }

    private Usuario usuario(UserDetails yo) { return usuarios.porEmail(yo.getUsername()); }
}
