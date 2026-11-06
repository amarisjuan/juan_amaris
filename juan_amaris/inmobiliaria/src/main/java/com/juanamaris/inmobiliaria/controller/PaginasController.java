package com.juanamaris.inmobiliaria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginasController {
    @GetMapping("/seguridad")
    public String seguridad() { return "seguridad"; }
}
