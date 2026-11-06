package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.model.Usuario;
import com.juanamaris.inmobiliaria.repository.UsuarioRepository;
import com.juanamaris.inmobiliaria.service.InmuebleService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/** Perfil público del anunciante: solo nombre público ("Laura M.") y sus inmuebles visibles. */
@Controller
public class PerfilController {

    private final UsuarioRepository usuarios;
    private final InmuebleService inmuebles;

    public PerfilController(UsuarioRepository usuarios, InmuebleService inmuebles) {
        this.usuarios = usuarios;
        this.inmuebles = inmuebles;
    }

    @GetMapping("/anunciantes/{id:\\d+}")
    public String perfil(@PathVariable Long id, Model model) {
        Usuario u = usuarios.findById(id).filter(x -> !x.isBloqueado())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Este perfil no está disponible en este momento."));
        model.addAttribute("anunciante", u);
        model.addAttribute("inmuebles", inmuebles.visiblesDe(u));
        return "perfil/anunciante";
    }
}
