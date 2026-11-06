package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.model.Companero;
import com.juanamaris.inmobiliaria.model.Inmueble;
import com.juanamaris.inmobiliaria.model.TipoEvento;
import com.juanamaris.inmobiliaria.service.ActividadService;
import com.juanamaris.inmobiliaria.service.CompaneroService;
import com.juanamaris.inmobiliaria.service.InmuebleService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.util.UriUtils;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Cuenta los contactos y los redirige a WhatsApp. Solo se cuenta el clic (no se sabe si hubo conversación)
 * y no se guarda quién hizo clic.
 */
@Controller
public class ContactoController {

    private final InmuebleService inmuebles;
    private final CompaneroService companeros;
    private final ActividadService actividad;

    public ContactoController(InmuebleService inmuebles, CompaneroService companeros, ActividadService actividad) {
        this.inmuebles = inmuebles;
        this.companeros = companeros;
        this.actividad = actividad;
    }

    @GetMapping("/contacto/inmueble/{id:\\d+}/whatsapp")
    public ResponseEntity<Void> whatsappInmueble(@PathVariable Long id) {
        Inmueble i = inmuebles.detallePublico(id);
        actividad.registrar(TipoEvento.CONTACTO_WHATSAPP, String.valueOf(id));
        return redirigir(whatsapp(i.getPropietario().getCelular(),
                "Hola, vi tu inmueble «" + i.getTitulo() + "» y me interesa."));
    }

    /** La llamada usa el enlace tel: directo; este endpoint solo la cuenta (lo llama el navegador en segundo plano). */
    @GetMapping("/contacto/inmueble/{id:\\d+}/llamada")
    public ResponseEntity<Void> llamadaInmueble(@PathVariable Long id) {
        inmuebles.detallePublico(id);
        actividad.registrar(TipoEvento.CONTACTO_LLAMADA, String.valueOf(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contacto/companero/{id:\\d+}/whatsapp")
    public ResponseEntity<Void> whatsappCompanero(@PathVariable Long id) {
        Companero c = companeros.activo(id);
        actividad.registrar(TipoEvento.CONTACTO_COMPANERO, String.valueOf(id));
        return redirigir(whatsapp(c.getAutor().getCelular(),
                "Hola " + c.getAutor().getNombre() + ", vi tu publicación de compañero de cuarto y me interesa."));
    }

    private static String whatsapp(String celular, String mensaje) {
        return "https://wa.me/57" + celular + "?text=" + UriUtils.encodeQueryParam(mensaje, StandardCharsets.UTF_8);
    }

    private static ResponseEntity<Void> redirigir(String url) {
        return ResponseEntity.status(302).location(URI.create(url)).build();
    }
}
