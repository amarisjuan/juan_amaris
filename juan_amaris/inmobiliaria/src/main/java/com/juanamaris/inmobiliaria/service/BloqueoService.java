package com.juanamaris.inmobiliaria.service;

import com.juanamaris.inmobiliaria.model.Bloqueo;
import com.juanamaris.inmobiliaria.model.Usuario;
import com.juanamaris.inmobiliaria.repository.BloqueoRepository;
import com.juanamaris.inmobiliaria.repository.RegistroAnuncianteRepository;
import com.juanamaris.inmobiliaria.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** Bloqueo (veto) de personas por correo y por documento. Lo decide siempre un administrador. */
@Service
public class BloqueoService {

    private final BloqueoRepository bloqueos;
    private final UsuarioRepository usuarios;
    private final RegistroAnuncianteRepository registros;

    public BloqueoService(BloqueoRepository bloqueos, UsuarioRepository usuarios, RegistroAnuncianteRepository registros) {
        this.bloqueos = bloqueos;
        this.usuarios = usuarios;
        this.registros = registros;
    }

    @Transactional(readOnly = true)
    public boolean correoBloqueado(String correo) {
        return bloqueos.existsByTipoAndValor(Bloqueo.Tipo.CORREO, Bloqueo.normalizarCorreo(correo));
    }

    @Transactional(readOnly = true)
    public boolean documentoBloqueado(String documento) {
        return bloqueos.existsByTipoAndValor(Bloqueo.Tipo.DOCUMENTO, Bloqueo.normalizarDocumento(documento));
    }

    /** Bloquea la cuenta y veta su correo y, si lo registró, su documento. */
    @Transactional
    public void bloquearUsuario(Long usuarioId, String motivo, String admin) {
        Usuario u = usuarios.findById(usuarioId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if ("ADMIN".equals(u.getRol())) throw new IllegalArgumentException("No se puede bloquear a un administrador");
        u.setBloqueado(true);
        vetar(Bloqueo.Tipo.CORREO, Bloqueo.normalizarCorreo(u.getEmail()), motivo, admin);
        registros.findByUsuario(u).ifPresent(r ->
                vetar(Bloqueo.Tipo.DOCUMENTO, Bloqueo.normalizarDocumento(r.getNumeroDocumento()), motivo, admin));
    }

    @Transactional
    public void desbloquearUsuario(Long usuarioId) {
        Usuario u = usuarios.findById(usuarioId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        u.setBloqueado(false);
        bloqueos.findByTipoAndValor(Bloqueo.Tipo.CORREO, Bloqueo.normalizarCorreo(u.getEmail())).ifPresent(bloqueos::delete);
        registros.findByUsuario(u).flatMap(r -> bloqueos.findByTipoAndValor(Bloqueo.Tipo.DOCUMENTO,
                Bloqueo.normalizarDocumento(r.getNumeroDocumento()))).ifPresent(bloqueos::delete);
    }

    /** Veto directo de un correo o documento (aunque no tenga cuenta). Si hay una cuenta con ese correo, se bloquea. */
    @Transactional
    public void vetarValor(Bloqueo.Tipo tipo, String valor, String motivo, String admin) {
        String v = tipo == Bloqueo.Tipo.CORREO ? Bloqueo.normalizarCorreo(valor) : Bloqueo.normalizarDocumento(valor);
        if (v.length() < 4) throw new IllegalArgumentException("Valor no válido");
        vetar(tipo, v, motivo, admin);
        if (tipo == Bloqueo.Tipo.CORREO) {
            usuarios.findByEmailIgnoreCase(v).filter(u -> !"ADMIN".equals(u.getRol())).ifPresent(u -> u.setBloqueado(true));
        } else {
            registros.findAll().stream().filter(r -> Bloqueo.normalizarDocumento(r.getNumeroDocumento()).equals(v))
                    .map(r -> r.getUsuario()).filter(u -> !"ADMIN".equals(u.getRol())).forEach(u -> u.setBloqueado(true));
        }
    }

    @Transactional
    public void quitarVeto(Long bloqueoId) { bloqueos.deleteById(bloqueoId); }

    @Transactional(readOnly = true)
    public List<Bloqueo> todos() { return bloqueos.findAllByOrderByFechaDesc(); }

    private void vetar(Bloqueo.Tipo tipo, String valor, String motivo, String admin) {
        if (valor.isBlank() || bloqueos.existsByTipoAndValor(tipo, valor)) return;
        bloqueos.save(new Bloqueo(tipo, valor, motivo, admin));
    }
}
