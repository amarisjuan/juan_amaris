package com.juanamaris.inmobiliaria.controller;

import com.juanamaris.inmobiliaria.model.TipoEvento;
import com.juanamaris.inmobiliaria.service.PanelService;
import com.juanamaris.inmobiliaria.service.RegistroAnuncianteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.EnumSet;

/** Panel del administrador. Toda la ruta /admin/** exige rol ADMIN (SeguridadConfig). */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PanelService panel;
    private final RegistroAnuncianteService registros;
    private final com.juanamaris.inmobiliaria.service.ReporteService reportes;
    private final com.juanamaris.inmobiliaria.service.BloqueoService bloqueos;
    private final com.juanamaris.inmobiliaria.repository.UsuarioRepository usuarios;

    public AdminController(PanelService panel, RegistroAnuncianteService registros,
                           com.juanamaris.inmobiliaria.service.ReporteService reportes,
                           com.juanamaris.inmobiliaria.service.BloqueoService bloqueos,
                           com.juanamaris.inmobiliaria.repository.UsuarioRepository usuarios) {
        this.reportes = reportes;
        this.bloqueos = bloqueos;
        this.usuarios = usuarios;
        this.panel = panel;
        this.registros = registros;
    }

    @GetMapping
    public String inicio(Model model) {
        var inmuebles = panel.rendimientoInmuebles();
        model.addAttribute("periodos", panel.periodos());
        model.addAttribute("totales", panel.totales());
        model.addAttribute("serieVisitas", paraGrafica(panel.serie(EnumSet.of(TipoEvento.VISITA_PAGINA), 30)));
        model.addAttribute("serieContactos", paraGrafica(panel.serie(PanelService.CONTACTOS, 30)));
        model.addAttribute("masContactados", inmuebles.stream().filter(f -> f.contactos() > 0).limit(10).toList());
        model.addAttribute("sinContacto", inmuebles.stream().filter(f -> f.contactos() == 0 && f.vistas() >= 3).limit(10).toList());
        model.addAttribute("sinResultados", panel.busquedas(TipoEvento.BUSQUEDA_SIN_RESULTADOS, 15));
        model.addAttribute("masBuscado", panel.busquedas(TipoEvento.BUSQUEDA, 10));
        model.addAttribute("hoy", panel.hoy());
        return "admin/panel";
    }

    private static final java.time.format.DateTimeFormatter CORTA =
            java.time.format.DateTimeFormatter.ofPattern("d MMM", new java.util.Locale("es", "CO"));

    /** Fechas ya formateadas para el JavaScript (evita depender de cómo se serializan las fechas). */
    private static java.util.List<java.util.Map<String, Object>> paraGrafica(java.util.List<PanelService.Dia> serie) {
        return serie.stream().map(d -> java.util.Map.<String, Object>of(
                "etiqueta", d.fecha().format(CORTA).replace(".", ""), "valor", d.valor())).toList();
    }

    // ---------- Reportes ----------

    @GetMapping("/reportes")
    public String reportes(Model model) {
        model.addAttribute("filas", reportes.resumen());
        model.addAttribute("pendientes", reportes.pendientes());
        return "admin/reportes";
    }

    @GetMapping("/reportes/usuario/{id}")
    public String reportesDeUsuario(@PathVariable Long id, Model model) {
        var u = usuarios.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        var lista = reportes.de(u);
        model.addAttribute("usuario", u);
        model.addAttribute("registro", registros.de(u).orElse(null));
        model.addAttribute("reportes", lista);
        java.util.Map<Long, String> titulos = new java.util.HashMap<>();
        lista.forEach(r -> titulos.put(r.getId(), reportes.tituloDe(r.getObjetivo(), r.getObjetivoId())));
        model.addAttribute("titulos", titulos);
        model.addAttribute("resultados", com.juanamaris.inmobiliaria.model.ResultadoReporte.values());
        return "admin/reportes_usuario";
    }

    @PostMapping("/reportes/{id}/resolver")
    public String resolver(@PathVariable Long id, @RequestParam com.juanamaris.inmobiliaria.model.ResultadoReporte resultado,
                           @RequestParam(defaultValue = "") String nota, @RequestParam Long usuario, RedirectAttributes flash) {
        reportes.resolver(id, resultado, nota.length() > 500 ? nota.substring(0, 500) : nota);
        flash.addFlashAttribute("ok", "Reporte resuelto: " + resultado.getEtiqueta());
        return "redirect:/admin/reportes/usuario/" + usuario;
    }

    @PostMapping("/publicaciones/ocultar")
    public String ocultar(@RequestParam com.juanamaris.inmobiliaria.model.ObjetivoReporte objetivo, @RequestParam Long id,
                          @RequestParam Long usuario, RedirectAttributes flash) {
        reportes.ocultar(objetivo, id, "En revisión por el equipo del portal");
        flash.addFlashAttribute("ok", "Publicación oculta mientras se revisa");
        return "redirect:/admin/reportes/usuario/" + usuario;
    }

    @PostMapping("/publicaciones/restaurar")
    public String restaurar(@RequestParam com.juanamaris.inmobiliaria.model.ObjetivoReporte objetivo, @RequestParam Long id,
                            @RequestParam Long usuario, RedirectAttributes flash) {
        reportes.restaurar(objetivo, id);
        flash.addFlashAttribute("ok", "Publicación visible de nuevo");
        return "redirect:/admin/reportes/usuario/" + usuario;
    }

    // ---------- Bloqueos ----------

    @PostMapping("/usuarios/{id}/bloquear")
    public String bloquear(@PathVariable Long id, @RequestParam String motivo, java.security.Principal admin, RedirectAttributes flash) {
        try {
            if (motivo.isBlank()) throw new IllegalArgumentException("Escribe el motivo del bloqueo");
            bloqueos.bloquearUsuario(id, motivo.trim(), admin.getName());
            flash.addFlashAttribute("ok", "Cuenta bloqueada. Se vetaron su correo y su documento.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reportes/usuario/" + id;
    }

    @PostMapping("/usuarios/{id}/desbloquear")
    public String desbloquear(@PathVariable Long id, RedirectAttributes flash) {
        bloqueos.desbloquearUsuario(id);
        flash.addFlashAttribute("ok", "Cuenta desbloqueada");
        return "redirect:/admin/reportes/usuario/" + id;
    }

    @GetMapping("/bloqueos")
    public String bloqueos(Model model) {
        model.addAttribute("bloqueos", bloqueos.todos());
        return "admin/bloqueos";
    }

    @PostMapping("/bloqueos")
    public String vetar(@RequestParam com.juanamaris.inmobiliaria.model.Bloqueo.Tipo tipo, @RequestParam String valor,
                        @RequestParam String motivo, java.security.Principal admin, RedirectAttributes flash) {
        try {
            if (motivo.isBlank()) throw new IllegalArgumentException("Escribe el motivo del bloqueo");
            bloqueos.vetarValor(tipo, valor, motivo.trim(), admin.getName());
            flash.addFlashAttribute("ok", "Bloqueo agregado");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/bloqueos";
    }

    @PostMapping("/bloqueos/{id}/eliminar")
    public String quitarVeto(@PathVariable Long id, RedirectAttributes flash) {
        bloqueos.quitarVeto(id);
        flash.addFlashAttribute("ok", "Bloqueo eliminado. Si había una cuenta bloqueada, desbloquéala desde sus reportes.");
        return "redirect:/admin/bloqueos";
    }

    @GetMapping("/anunciantes")
    public String anunciantes(Model model) {
        model.addAttribute("registros", registros.todos());
        return "admin/anunciantes";
    }
    /** Cantidad de reportes pendientes para la pestaña "Reportes" en todas las páginas del panel. */
    @org.springframework.web.bind.annotation.ModelAttribute("reportesPendientes")
    public long reportesPendientes() {
        return reportes.pendientes();
    }
}
