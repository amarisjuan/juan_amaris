package com.juanamaris.inmobiliaria.config;

import com.juanamaris.inmobiliaria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Cierra la sesión en el siguiente clic si la cuenta se bloqueó, o si la contraseña se cambió después de que esta
 * sesión ingresó (recuperación de contraseña: se cierran los demás dispositivos).
 */
public class SesionBloqueada implements HandlerInterceptor {

    /** Atributo de sesión con la hora del ingreso (lo pone el login). */
    public static final String INGRESO = "nidal.ingreso";

    private final UsuarioRepository usuarios;

    public SesionBloqueada(UsuarioRepository usuarios) { this.usuarios = usuarios; }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken) return true;
        var cuenta = usuarios.findByEmailIgnoreCase(a.getName()).orElse(null);
        if (cuenta == null) return true;
        var sesion = req.getSession(false);
        String destino = null;
        if (cuenta.isBloqueado()) destino = "/login?bloqueado";
        else if (cuenta.getCredencialesCambiadasEn() != null) {
            Object ingreso = sesion == null ? null : sesion.getAttribute(INGRESO);
            if (!(ingreso instanceof java.time.LocalDateTime t) || t.isBefore(cuenta.getCredencialesCambiadasEn())) destino = "/login?sesion";
        }
        if (destino == null) return true;
        SecurityContextHolder.clearContext();
        if (sesion != null) sesion.invalidate();
        res.sendRedirect(req.getContextPath() + destino);
        return false;
    }
}
