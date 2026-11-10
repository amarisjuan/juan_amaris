package com.juanamaris.inmobiliaria.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita los intentos fallidos de ingreso (recomendación de JDoor): por correo (5 en 15 min) y por dirección IP
 * (30 en 15 min, contra quien prueba muchas cuentas). Funciona igual exista o no la cuenta, así no revela cuáles
 * existen. Es temporal (no bloquea la cuenta para siempre) y la recuperación de contraseña sigue funcionando.
 * En memoria: se reinicia al reiniciar el portal (suficiente para un solo servidor).
 */
@Service
public class IntentosIngreso {

    private static final Logger log = LoggerFactory.getLogger(IntentosIngreso.class);
    private final Map<String, Deque<Instant>> fallos = new ConcurrentHashMap<>();
    private final Clock reloj;

    @Value("${app.ingreso.max-fallos-cuenta:5}") private int maxPorCuenta;
    @Value("${app.ingreso.max-fallos-ip:30}") private int maxPorIp;
    @Value("${app.ingreso.ventana-minutos:15}") private int ventanaMinutos;

    public IntentosIngreso(Clock reloj) { this.reloj = reloj; }

    private static String cuenta(String email) { return "c:" + (email == null ? "" : email.trim().toLowerCase(Locale.ROOT)); }
    private static String ip(String ip) { return "i:" + ip; }

    private int recientes(String clave) {
        Deque<Instant> d = fallos.get(clave);
        if (d == null) return 0;
        Instant limite = Instant.now(reloj).minusSeconds(ventanaMinutos * 60L);
        synchronized (d) {
            while (!d.isEmpty() && d.peekFirst().isBefore(limite)) d.pollFirst();
            return d.size();
        }
    }

    public boolean bloqueado(String email, String direccionIp) {
        return recientes(cuenta(email)) >= maxPorCuenta || recientes(ip(direccionIp)) >= maxPorIp;
    }

    public void fallo(String email, String direccionIp) {
        Instant ahora = Instant.now(reloj);
        for (String k : new String[] {cuenta(email), ip(direccionIp)}) {
            Deque<Instant> d = fallos.computeIfAbsent(k, x -> new ArrayDeque<>());
            synchronized (d) { d.addLast(ahora); }
        }
        // Registro sin correo, contraseña ni IP completa (datos personales).
        if (recientes(cuenta(email)) == maxPorCuenta) log.warn("Ingreso: una cuenta llegó al límite de intentos fallidos; pausa de {} min", ventanaMinutos);
        if (recientes(ip(direccionIp)) == maxPorIp) log.warn("Ingreso: una dirección llegó al límite de intentos fallidos; pausa de {} min", ventanaMinutos);
    }

    public void exito(String email) { fallos.remove(cuenta(email)); }

    /** Limpia entradas viejas cada hora para no acumular memoria. */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 3_600_000)
    public void limpiar() { fallos.keySet().forEach(k -> { if (recientes(k) == 0) fallos.remove(k); }); }
}
