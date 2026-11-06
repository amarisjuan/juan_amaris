package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.model.Companero;
import com.juanamaris.inmobiliaria.model.Inmueble;
import com.juanamaris.inmobiliaria.model.TipoEvento;
import com.juanamaris.inmobiliaria.service.ActividadService;
import com.juanamaris.inmobiliaria.service.CompaneroService;
import com.juanamaris.inmobiliaria.service.InmuebleService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    private final String nombrePortal;

    public ContactoController(InmuebleService inmuebles, CompaneroService companeros, ActividadService actividad,
                              @org.springframework.beans.factory.annotation.Value("${app.nombre-portal}") String nombrePortal) {
        this.nombrePortal = nombrePortal;
        this.inmuebles = inmuebles;
        this.companeros = companeros;
        this.actividad = actividad;
    }

    /** Pantalla intermedia con el aviso antiestafa (consulta legal 003) antes de abrir WhatsApp. */
    @GetMapping("/contacto/inmueble/{id:\\d+}/whatsapp")
    public String avisoWhatsappInmueble(@PathVariable Long id, Model model) {
        Inmueble i = inmuebles.detallePublico(id);
        model.addAttribute("titulo", i.getTitulo());
        model.addAttribute("volver", "/inmuebles/" + id);
        model.addAttribute("abrir", "/contacto/inmueble/" + id + "/whatsapp/abrir");
        model.addAttribute("canal", "WhatsApp");
        return "contacto/aviso";
    }

    @GetMapping("/contacto/inmueble/{id:\\d+}/whatsapp/abrir")
    public ResponseEntity<Void> whatsappInmueble(@PathVariable Long id) {
        Inmueble i = inmuebles.detallePublico(id);
        actividad.registrar(TipoEvento.CONTACTO_WHATSAPP, String.valueOf(id));
        // Mensaje inicial neutro: sin el aviso adentro y sin datos del anunciante
        return redirigir(whatsapp(i.getPropietario().getCelular(),
                "Hola, vi tu anuncio «" + i.getTitulo() + "» en " + nombrePortal + " y me interesa."));
    }

    @GetMapping("/contacto/inmueble/{id:\\d+}/llamar")
    public String avisoLlamada(@PathVariable Long id, Model model) {
        Inmueble i = inmuebles.detallePublico(id);
        model.addAttribute("titulo", i.getTitulo());
        model.addAttribute("volver", "/inmuebles/" + id);
        model.addAttribute("telefono", "tel:+57" + i.getPropietario().getCelular());
        model.addAttribute("contador", "/contacto/inmueble/" + id + "/llamada");
        model.addAttribute("canal", "llamada");
        return "contacto/aviso";
    }

    /** Solo cuenta la llamada (la página de aviso lo llama en segundo plano al tocar "Llamar"). */
    @GetMapping("/contacto/inmueble/{id:\\d+}/llamada")
    public ResponseEntity<Void> llamadaInmueble(@PathVariable Long id) {
        inmuebles.detallePublico(id);
        actividad.registrar(TipoEvento.CONTACTO_LLAMADA, String.valueOf(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contacto/companero/{id:\\d+}/whatsapp")
    public String avisoWhatsappCompanero(@PathVariable Long id, Model model) {
        Companero c = companeros.activo(id);
        model.addAttribute("titulo", c.getTipo().getEtiqueta() + " · " + c.getUniversidad().getNombreCorto());
        model.addAttribute("volver", "/companeros");
        model.addAttribute("abrir", "/contacto/companero/" + id + "/whatsapp/abrir");
        model.addAttribute("canal", "WhatsApp");
        return "contacto/aviso";
    }

    @GetMapping("/contacto/companero/{id:\\d+}/whatsapp/abrir")
    public ResponseEntity<Void> whatsappCompanero(@PathVariable Long id) {
        Companero c = companeros.activo(id);
        actividad.registrar(TipoEvento.CONTACTO_COMPANERO, String.valueOf(id));
        return redirigir(whatsapp(c.getAutor().getCelular(),
                "Hola, vi tu publicación de compañero de cuarto en " + nombrePortal + " y me interesa."));
    }

    private static String whatsapp(String celular, String mensaje) {
        return "https://wa.me/57" + celular + "?text=" + UriUtils.encodeQueryParam(mensaje, StandardCharsets.UTF_8);
    }

    private static ResponseEntity<Void> redirigir(String url) {
        return ResponseEntity.status(302).location(URI.create(url)).build();
    }
}
