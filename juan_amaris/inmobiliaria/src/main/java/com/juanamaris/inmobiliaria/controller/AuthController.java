package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.model.Usuario;
import com.juanamaris.inmobiliaria.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioService usuarios;
    private final com.juanamaris.inmobiliaria.service.BloqueoService bloqueos;

    private final com.juanamaris.inmobiliaria.service.ValidadorContrasena validador;

    public AuthController(UsuarioService usuarios, com.juanamaris.inmobiliaria.service.BloqueoService bloqueos,
                          com.juanamaris.inmobiliaria.service.ValidadorContrasena validador) {
        this.usuarios = usuarios;
        this.bloqueos = bloqueos;
        this.validador = validador;
    }

    @InitBinder("usuario")
    public void protegerCampos(WebDataBinder binder) {
        binder.setAllowedFields("nombres", "apellidos", "email", "celular");
    }

    @GetMapping("/login")
    public String login() { return "auth/login"; }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute Usuario usuario, BindingResult errores,
                            @RequestParam String clave, RedirectAttributes flash) {
        String problemaClave = validador.revisar(clave, usuario.getEmail());
        if (problemaClave != null) errores.reject("clave", problemaClave);
        if (!errores.hasFieldErrors("email") && bloqueos.correoBloqueado(usuario.getEmail())) {
            errores.rejectValue("email", "bloqueado", "No es posible crear una cuenta con este correo.");
        }
        if (!errores.hasFieldErrors("email") && usuarios.existeEmail(usuario.getEmail())) {
            errores.rejectValue("email", "duplicado", "Ya existe una cuenta con ese correo");
        }
        if (errores.hasErrors()) return "auth/registro";
        usuarios.registrar(usuario, clave);
        flash.addFlashAttribute("ok", "Cuenta creada. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }
}
