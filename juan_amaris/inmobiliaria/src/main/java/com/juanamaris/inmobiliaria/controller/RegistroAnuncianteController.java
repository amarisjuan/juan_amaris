package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.model.RegistroAnunciante;
import com.juanamaris.inmobiliaria.model.TipoDocumento;
import com.juanamaris.inmobiliaria.model.Usuario;
import com.juanamaris.inmobiliaria.service.RegistroAnuncianteService;
import com.juanamaris.inmobiliaria.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** El anunciante llena (o actualiza) sus datos privados del art. 53 antes de publicar inmuebles. */
@Controller
@RequestMapping("/mi-registro")
public class RegistroAnuncianteController {

    private final RegistroAnuncianteService registros;
    private final UsuarioService usuarios;

    public RegistroAnuncianteController(RegistroAnuncianteService registros, UsuarioService usuarios) {
        this.registros = registros;
        this.usuarios = usuarios;
    }

    @InitBinder("registro")
    public void protegerCampos(WebDataBinder binder) {
        binder.setAllowedFields("tipoDocumento", "numeroDocumento", "direccionNotificaciones", "ciudadNotificaciones");
    }

    @ModelAttribute("tiposDocumento")
    public TipoDocumento[] tipos() { return TipoDocumento.values(); }

    @GetMapping
    public String ver(@AuthenticationPrincipal UserDetails yo, Model model) {
        Usuario u = usuarios.porEmail(yo.getUsername());
        model.addAttribute("registro", registros.de(u).orElseGet(RegistroAnunciante::new));
        model.addAttribute("celular", u.getCelular());
        return "inmuebles/registro_anunciante";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("registro") RegistroAnunciante registro, BindingResult errores,
                          @RequestParam(defaultValue = "false") boolean declaro,
                          @AuthenticationPrincipal UserDetails yo, Model model, RedirectAttributes flash) {
        Usuario u = usuarios.porEmail(yo.getUsername());
        if (!declaro) errores.reject("declaro", "Debes declarar que los datos son verdaderos");
        if (!errores.hasFieldErrors("numeroDocumento") && registro.getNumeroDocumento() != null
                && registros.documentoVetado(u, registro.getNumeroDocumento())) {
            return "redirect:/login?bloqueado";
        }
        if (errores.hasErrors()) {
            model.addAttribute("celular", u.getCelular());
            return "inmuebles/registro_anunciante";
        }
        registros.guardar(u, registro);
        flash.addFlashAttribute("ok", "Datos de anunciante guardados. Ya puedes publicar.");
        return "redirect:/publicar";
    }
}
