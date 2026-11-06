package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.model.MotivoReporte;
import com.juanamaris.inmobiliaria.model.ObjetivoReporte;
import com.juanamaris.inmobiliaria.service.ReporteService;
import com.juanamaris.inmobiliaria.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** Reportes confidenciales: solo con sesión iniciada (SeguridadConfig: anyRequest().authenticated()). */
@Controller
@RequestMapping("/reportar")
public class ReporteController {

    private final ReporteService reportes;
    private final UsuarioService usuarios;

    public ReporteController(ReporteService reportes, UsuarioService usuarios) {
        this.reportes = reportes;
        this.usuarios = usuarios;
    }

    @GetMapping
    public String formulario(@RequestParam ObjetivoReporte objetivo, @RequestParam Long id, Model model) {
        if (reportes.duenoDe(objetivo, id) == null) return "redirect:/";
        llenar(model, objetivo, id);
        return "reportes/formulario";
    }

    @PostMapping
    public String enviar(@RequestParam ObjetivoReporte objetivo, @RequestParam Long id,
                         @RequestParam(required = false) MotivoReporte motivo,
                         @RequestParam(defaultValue = "") String descripcion,
                         @AuthenticationPrincipal UserDetails yo, Model model) {
        String error = motivo == null ? "Elige un motivo."
                : descripcion.isBlank() ? "Describe lo que pasó."
                : descripcion.length() > 1000 ? "La descripción tiene máximo 1000 caracteres." : null;
        if (error == null) {
            try {
                reportes.reportar(usuarios.porEmail(yo.getUsername()), objetivo, id, motivo, descripcion);
            } catch (IllegalArgumentException e) {
                error = e.getMessage();
            }
        }
        if (error != null) {
            llenar(model, objetivo, id);
            model.addAttribute("error", error);
            model.addAttribute("motivoElegido", motivo);
            model.addAttribute("descripcion", descripcion);
            return "reportes/formulario";
        }
        model.addAttribute("estafa", motivo == MotivoReporte.ESTAFA);
        return "reportes/gracias";
    }

    private void llenar(Model model, ObjetivoReporte objetivo, Long id) {
        model.addAttribute("objetivo", objetivo);
        model.addAttribute("id", id);
        model.addAttribute("titulo", reportes.tituloDe(objetivo, id));
        model.addAttribute("motivos", MotivoReporte.values());
    }
}
