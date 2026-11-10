package com.juanamaris.inmobiliaria.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Envía correos por Brevo (SMTP). El envío va en segundo plano: así la respuesta tarda lo mismo exista o no la
 * cuenta, y un correo lento no bloquea la página. Sin credenciales configuradas no envía nada: deja el aviso en la
 * consola (modo desarrollo) para poder probar en local.
 */
@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);
    private final ObjectProvider<JavaMailSender> mailSender;
    private final TemplateEngine plantillas;
    private final ExecutorService cola = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "envio-correos");
        t.setDaemon(true);
        return t;
    });

    @Value("${spring.mail.username:}") private String usuarioSmtp;
    @Value("${app.correo.remitente}") private String remitente;
    @Value("${app.correo.nombre-remitente}") private String nombreRemitente;

    public CorreoService(ObjectProvider<JavaMailSender> mailSender, TemplateEngine plantillas) {
        this.mailSender = mailSender;
        this.plantillas = plantillas;
    }

    public boolean configurado() { return usuarioSmtp != null && !usuarioSmtp.isBlank(); }

    /** Arma el correo con la plantilla templates/correo/{plantilla}.html y lo envía en segundo plano. */
    public void enviar(String para, String asunto, String plantilla, Map<String, Object> datos) {
        Context ctx = new Context(new java.util.Locale("es", "CO"));
        ctx.setVariables(datos);
        String html = plantillas.process("correo/" + plantilla, ctx);
        if (!configurado()) {
            log.warn("[CORREO NO ENVIADO - modo desarrollo, falta BREVO_SMTP_USUARIO] Para: {} | Asunto: {} | Enlace: {}",
                    para, asunto, datos.getOrDefault("enlace", "(sin enlace)"));
            return;
        }
        cola.submit(() -> {
            try {
                MimeMessage m = mailSender.getObject().createMimeMessage();
                MimeMessageHelper h = new MimeMessageHelper(m, false, "UTF-8");
                h.setFrom(new InternetAddress(remitente, nombreRemitente, "UTF-8"));
                h.setTo(para);
                h.setSubject(asunto);
                h.setText(html, true);
                mailSender.getObject().send(m);
                log.info("Correo enviado: {}", asunto);
            } catch (Exception e) {
                // No se registra la dirección ni el contenido (datos personales y enlace secreto).
                log.error("No se pudo enviar el correo '{}': {}", asunto, e.getMessage());
            }
        });
    }
}
