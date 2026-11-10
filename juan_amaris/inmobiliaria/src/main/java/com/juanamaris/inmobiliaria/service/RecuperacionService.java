package com.juanamaris.inmobiliaria.service;

import com.juanamaris.inmobiliaria.model.TokenRecuperacion;
import com.juanamaris.inmobiliaria.model.Usuario;
import com.juanamaris.inmobiliaria.repository.TokenRecuperacionRepository;
import com.juanamaris.inmobiliaria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

/** Recuperación de contraseña por correo: enlace de un solo uso, con vencimiento y límite de solicitudes. */
@Service
public class RecuperacionService {

    private final UsuarioRepository usuarios;
    private final TokenRecuperacionRepository tokens;
    private final PasswordEncoder encoder;
    private final CorreoService correo;
    private final Clock reloj;
    private final SecureRandom azar = new SecureRandom();

    @Value("${app.recuperacion.minutos-vigencia:30}") private int minutosVigencia;
    @Value("${app.recuperacion.max-por-hora:3}") private int maxPorHora;
    @Value("${app.url-base}") private String urlBase;

    public RecuperacionService(UsuarioRepository usuarios, TokenRecuperacionRepository tokens, PasswordEncoder encoder,
                               CorreoService correo, Clock reloj) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.encoder = encoder;
        this.correo = correo;
        this.reloj = reloj;
    }

    private LocalDateTime ahora() { return LocalDateTime.now(reloj); }

    static String hash(String codigo) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(codigo.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Si el correo es de una cuenta activa y no superó el límite, crea un enlace y lo envía.
     * Devuelve el código solo para las pruebas; quien llama no debe mostrarlo nunca.
     */
    @Transactional
    public Optional<String> solicitar(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        Optional<Usuario> cuenta = usuarios.findByEmailIgnoreCase(email.trim());
        if (cuenta.isEmpty() || cuenta.get().isBloqueado()) return Optional.empty();
        Usuario u = cuenta.get();
        LocalDateTime ahora = ahora();
        if (tokens.countByUsuarioAndCreadoEnAfter(u, ahora.minusHours(1)) >= maxPorHora) return Optional.empty();
        // Solo vale el enlace más reciente.
        tokens.findByUsuarioAndUsadoEnIsNull(u).forEach(t -> t.setUsadoEn(ahora));
        byte[] bytes = new byte[32];
        azar.nextBytes(bytes);
        String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.save(new TokenRecuperacion(u, hash(codigo), ahora, ahora.plusMinutes(minutosVigencia)));
        String enlace = urlBase.replaceAll("/+$", "") + "/restablecer?codigo=" + codigo;
        correo.enviar(u.getEmail(), "Restablece tu contraseña de Nidal Inmuebles", "recuperar",
                Map.of("nombre", u.getNombres() == null ? "" : u.getNombres(), "enlace", enlace, "minutos", minutosVigencia));
        return Optional.of(codigo);
    }

    /** true si el código existe, no se ha usado y no ha vencido. */
    @Transactional(readOnly = true)
    public boolean valido(String codigo) {
        return buscar(codigo).map(t -> t.vigente(ahora()) && !t.getUsuario().isBloqueado()).orElse(false);
    }

    /** Correo de la cuenta del enlace (para no permitir que la contraseña sea el propio correo). */
    @Transactional(readOnly = true)
    public String correoDe(String codigo) {
        return buscar(codigo).map(t -> t.getUsuario().getEmail()).orElse(null);
    }

    private Optional<TokenRecuperacion> buscar(String codigo) {
        if (codigo == null || codigo.length() < 20 || codigo.length() > 100) return Optional.empty();
        return tokens.findByHash(hash(codigo));
    }

    /** Cambia la contraseña. Devuelve false si el enlace ya no sirve. */
    @Transactional
    public boolean restablecer(String codigo, String claveNueva) {
        Optional<TokenRecuperacion> t = buscar(codigo);
        LocalDateTime ahora = ahora();
        if (t.isEmpty() || !t.get().vigente(ahora) || t.get().getUsuario().isBloqueado()) return false;
        Usuario u = usuarios.findById(t.get().getUsuario().getId()).orElseThrow();
        u.setPassword(encoder.encode(claveNueva));
        u.setCredencialesCambiadasEn(ahora);
        usuarios.save(u);
        tokens.findByUsuarioAndUsadoEnIsNull(u).forEach(x -> x.setUsadoEn(ahora));
        correo.enviar(u.getEmail(), "Tu contraseña de Nidal Inmuebles se cambió", "contrasena_cambiada",
                Map.of("nombre", u.getNombres() == null ? "" : u.getNombres(),
                        "enlace", urlBase.replaceAll("/+$", "") + "/recuperar"));
        return true;
    }

    /** Borra los enlaces vencidos hace más de un día (se llama al arrancar y cada noche). */
    @Transactional
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 15 3 * * *", zone = "America/Bogota")
    public void limpiar() { tokens.deleteByVenceEnBefore(ahora().minusDays(1)); }
}
