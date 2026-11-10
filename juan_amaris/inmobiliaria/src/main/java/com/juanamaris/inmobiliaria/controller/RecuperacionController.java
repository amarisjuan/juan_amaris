package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.service.RecuperacionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RecuperacionController {

    private final RecuperacionService recuperacion;

    private final com.juanamaris.inmobiliaria.service.ValidadorContrasena validador;

    public RecuperacionController(RecuperacionService recuperacion, com.juanamaris.inmobiliaria.service.ValidadorContrasena validador) {
        this.recuperacion = recuperacion;
        this.validador = validador;
    }

    @GetMapping("/recuperar")
    public String formulario() { return "auth/recuperar"; }

    /** Siempre la misma respuesta: no revela si el correo tiene cuenta. */
    @PostMapping("/recuperar")
    public String solicitar(@RequestParam(defaultValue = "") String email, RedirectAttributes flash) {
        recuperacion.solicitar(email);
        flash.addFlashAttribute("enviado", true);
        return "redirect:/recuperar";
    }

    @GetMapping("/restablecer")
    public String restablecer(@RequestParam(defaultValue = "") String codigo, Model model, HttpServletResponse res) {
        // El código va en la dirección: que no se filtre a otros sitios ni quede en caché.
        res.setHeader("Referrer-Policy", "no-referrer");
        res.setHeader("Cache-Control", "no-store");
        model.addAttribute("codigo", codigo);
        model.addAttribute("valido", recuperacion.valido(codigo));
        return "auth/restablecer";
    }

    @PostMapping("/restablecer")
    public String guardar(@RequestParam(defaultValue = "") String codigo, @RequestParam(defaultValue = "") String clave,
                          @RequestParam(defaultValue = "") String clave2, Model model, RedirectAttributes flash,
                          HttpServletResponse res) {
        res.setHeader("Referrer-Policy", "no-referrer");
        res.setHeader("Cache-Control", "no-store");
        String error = validador.revisar(clave, recuperacion.correoDe(codigo));
        if (error == null && !clave.equals(clave2)) error = "Las dos contraseñas no coinciden.";
        if (error != null) {
            model.addAttribute("codigo", codigo);
            model.addAttribute("valido", recuperacion.valido(codigo));
            model.addAttribute("error", error);
            return "auth/restablecer";
        }
        if (!recuperacion.restablecer(codigo, clave)) {
            model.addAttribute("codigo", "");
            model.addAttribute("valido", false);
            return "auth/restablecer";
        }
        flash.addFlashAttribute("ok", "Listo, tu contraseña se cambió. Ya puedes ingresar con la nueva.");
        return "redirect:/login";
    }
}
