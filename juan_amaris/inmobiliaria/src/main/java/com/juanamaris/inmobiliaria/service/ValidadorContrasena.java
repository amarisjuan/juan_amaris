package com.juanamaris.inmobiliaria.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reglas de contraseña (recomendación de JDoor, NIST 800-63B / OWASP ASVS): entre 8 y 64 caracteres, sin reglas de
 * composición, pero no puede ser una contraseña común, ni repetir un solo carácter, ni ser el propio correo.
 */
@Service
public class ValidadorContrasena {

    public static final int MINIMO = 8;
    public static final int MAXIMO = 64;
    private final Set<String> comunes;

    public ValidadorContrasena() throws IOException {
        String texto = new String(new ClassPathResource("datos/contrasenas_comunes.txt").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);
        comunes = texto.lines().map(String::trim).filter(l -> !l.isEmpty() && !l.startsWith("#"))
                .map(l -> l.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
    }

    /** Devuelve el mensaje de error, o null si la contraseña sirve. */
    public String revisar(String clave, String email) {
        if (clave == null || clave.length() < MINIMO) return "La contraseña debe tener mínimo " + MINIMO + " caracteres.";
        if (clave.length() > MAXIMO) return "La contraseña puede tener máximo " + MAXIMO + " caracteres.";
        String c = clave.toLowerCase(Locale.ROOT);
        if (comunes.contains(c) || c.chars().distinct().count() == 1)
            return "Esa contraseña es muy común y fácil de adivinar. Usa otra (por ejemplo, tres palabras que recuerdes).";
        if (email != null && !email.isBlank()) {
            String e = email.trim().toLowerCase(Locale.ROOT);
            String usuario = e.contains("@") ? e.substring(0, e.indexOf('@')) : e;
            if (c.equals(e) || (usuario.length() >= 4 && c.equals(usuario)))
                return "La contraseña no puede ser tu correo.";
        }
        return null;
    }
}
