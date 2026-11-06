package com.juanamaris.inmobiliaria.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Muestra nuestras páginas de "no disponible" con el mensaje neutro definido en el código
 * (nunca "reportado" ni detalles internos). Otros errores siguen el manejo normal de Spring.
 */
@ControllerAdvice
public class ManejadorNoDisponible {

    @ExceptionHandler(ResponseStatusException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noDisponible(ResponseStatusException e, Model model) {
        if (e.getStatusCode().value() != 404) throw e;
        model.addAttribute("message", e.getReason());
        return "error/404";
    }
}
