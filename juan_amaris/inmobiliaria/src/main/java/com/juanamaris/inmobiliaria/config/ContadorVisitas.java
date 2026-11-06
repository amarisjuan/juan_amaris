package com.juanamaris.inmobiliaria.config;

import com.juanamaris.inmobiliaria.service.ActividadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.regex.Pattern;

/** Cuenta páginas vistas (solo GET a páginas del portal; no estáticos, admin, contactos ni robots). */
public class ContadorVisitas implements HandlerInterceptor {

    private static final Pattern ROBOT = Pattern.compile("(?i)bot|crawl|spider|slurp|preview|monitor|headless");
    private final ActividadService actividad;

    public ContadorVisitas(ActividadService actividad) { this.actividad = actividad; }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        if (!"GET".equals(req.getMethod()) || !(handler instanceof HandlerMethod) || res.getStatus() != 200) return;
        String ruta = req.getRequestURI();
        if (ruta.startsWith("/admin") || ruta.startsWith("/contacto") || ruta.startsWith("/error")) return;
        String navegador = req.getHeader("User-Agent");
        if (navegador == null || ROBOT.matcher(navegador).find()) return;
        actividad.registrarVisita(req.getRemoteAddr(), navegador);
    }
}
