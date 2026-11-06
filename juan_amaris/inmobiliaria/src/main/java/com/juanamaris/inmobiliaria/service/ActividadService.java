package com.juanamaris.inmobiliaria.service;

import com.juanamaris.inmobiliaria.model.ConteoDiario;
import com.juanamaris.inmobiliaria.model.TipoEvento;
import com.juanamaris.inmobiliaria.repository.ConteoDiarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;

/**
 * Cuenta la actividad del portal de forma agregada y sin identificar a nadie.
 * Visitantes únicos: huella = SHA-256(sal_del_día + IP + navegador). La sal es aleatoria, vive solo en memoria
 * y se cambia cada día, así que la huella no se puede revertir ni cruzar entre días. Ni la IP ni la huella se guardan
 * en la base: solo el total. (Diseño pendiente de revisión de Argenis, consulta 002.)
 */
@Service
public class ActividadService {

    private final ConteoDiarioRepository repo;
    private final Clock reloj;
    private final SecureRandom azar = new SecureRandom();
    private LocalDate diaActual;
    private byte[] salDelDia;
    private final Set<String> huellasDelDia = new HashSet<>();

    public ActividadService(ConteoDiarioRepository repo, Clock reloj) {
        this.repo = repo;
        this.reloj = reloj;
    }

    @Transactional
    public synchronized void registrar(TipoEvento tipo, String clave) {
        String k = clave == null ? "" : (clave.length() > 200 ? clave.substring(0, 200) : clave);
        LocalDate hoy = LocalDate.now(reloj);
        ConteoDiario c = repo.findByFechaAndTipoAndClave(hoy, tipo, k).orElseGet(() -> new ConteoDiario(hoy, tipo, k));
        c.sumar();
        repo.save(c);
    }

    /** Registra una página vista y, si es la primera vez en el día para esa huella, un visitante. */
    @Transactional
    public synchronized void registrarVisita(String ip, String navegador) {
        registrar(TipoEvento.VISITA_PAGINA, "");
        rotarSiCambioElDia();
        if (huellasDelDia.add(huella(ip, navegador))) registrar(TipoEvento.VISITANTE, "");
    }

    private void rotarSiCambioElDia() {
        LocalDate hoy = LocalDate.now(reloj);
        if (!hoy.equals(diaActual)) {
            diaActual = hoy;
            salDelDia = new byte[32];
            azar.nextBytes(salDelDia);
            huellasDelDia.clear();
        }
    }

    private String huella(String ip, String navegador) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(salDelDia);
            sha.update(String.valueOf(ip).getBytes(StandardCharsets.UTF_8));
            sha.update((byte) 0);
            sha.update(String.valueOf(navegador).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(sha.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
