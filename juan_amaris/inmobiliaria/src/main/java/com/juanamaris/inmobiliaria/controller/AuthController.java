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

    public AuthController(UsuarioService usuarios) { this.usuarios = usuarios; }

    @InitBinder("usuario")
    public void protegerCampos(WebDataBinder binder) {
        binder.setDisallowedFields("id", "rol", "password");
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
        if (clave.length() < 8) {
            errores.reject("clave", "La contraseña debe tener mínimo 8 caracteres");
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
