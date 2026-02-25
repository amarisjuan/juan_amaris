package com.desarrolloweb.inventario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

    @GetMapping({"/", "/login"})
    public String login(Model model) {
        model.addAttribute("titulo", "Login");
        return "login";
    }
}
