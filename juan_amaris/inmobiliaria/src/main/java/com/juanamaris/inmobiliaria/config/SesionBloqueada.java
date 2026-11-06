package com.juanamaris.inmobiliaria.config;

import com.juanamaris.inmobiliaria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

/** Si una cuenta se bloquea mientras tiene la sesión abierta, la cierra en su siguiente clic. */
public class SesionBloqueada implements HandlerInterceptor {

    private final UsuarioRepository usuarios;

    public SesionBloqueada(UsuarioRepository usuarios) { this.usuarios = usuarios; }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken) return true;
        boolean bloqueado = usuarios.findByEmailIgnoreCase(a.getName()).map(u -> u.isBloqueado()).orElse(false);
        if (!bloqueado) return true;
        SecurityContextHolder.clearContext();
        var sesion = req.getSession(false);
        if (sesion != null) sesion.invalidate();
        res.sendRedirect(req.getContextPath() + "/login?bloqueado");
        return false;
    }
}
