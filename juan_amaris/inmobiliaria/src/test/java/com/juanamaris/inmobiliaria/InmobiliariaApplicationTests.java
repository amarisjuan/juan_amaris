package com.juanamaris.inmobiliaria;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        "app.uploads.dir=target/test-uploads"})
@AutoConfigureMockMvc
class InmobiliariaApplicationTests {

    @Autowired MockMvc mvc;
    @Autowired com.juanamaris.inmobiliaria.repository.UniversidadRepository universidades;

    @Test
    void estudiantesBuscanCercaASuUniversidad() throws Exception {
        Long uninorte = universidades.findBySigla("Uninorte").orElseThrow().getId();
        mvc.perform(get("/").param("universidad", uninorte.toString()).param("maxMinutos", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Habitación amoblada a pasos de Uninorte")))
                .andExpect(content().string(not(containsString("Apartaestudio para estudiantes cerca a la CUC"))));

        // Orden "más cerca": Uninorte (8 min) debe salir antes que la CUC (12 min)
        String html = mvc.perform(get("/").param("orden", "cercania"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        html = html.substring(html.indexOf("class=\"rejilla\""));   // solo la lista, no los datos del mapa
        org.junit.jupiter.api.Assertions.assertTrue(
                html.indexOf("Habitación amoblada a pasos de Uninorte") < html.indexOf("Apartaestudio para estudiantes cerca a la CUC"));

        mvc.perform(get("/estudiantes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Universidad del Norte")));
    }

    @Test
    @WithMockUser(username = "admin@inmobiliaria.co")
    void publicarConUniversidadCercana() throws Exception {
        Long udea = universidades.findBySigla("UdeA").orElseThrow().getId();
        mvc.perform(multipart("/publicar").with(csrf())
                        .param("titulo", "Habitación junto a la UdeA").param("operacion", "ARRIENDO")
                        .param("tipo", "HABITACION").param("precio", "700000").param("ciudad", "Medellín")
                        .param("universidadCercana", udea.toString()).param("minutosAPie", "5")
                        .param("amoblado", "true").param("latitud", "6.2690").param("longitud", "-75.5660"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/").param("universidad", udea.toString()).param("amoblado", "true"))
                .andExpect(content().string(containsString("Habitación junto a la UdeA")))
                .andExpect(content().string(containsString("5 min a pie de ")));

        // El detalle muestra la universidad y el mapa
        String html = mvc.perform(get("/").param("universidad", udea.toString()))
                .andReturn().getResponse().getContentAsString();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("href=\"/inmuebles/(\\d+)\"").matcher(html);
        org.junit.jupiter.api.Assertions.assertTrue(m.find());
        mvc.perform(get("/inmuebles/" + m.group(1)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Universidad de Antioquia")))
                .andExpect(content().string(containsString("mapa-detalle")));
    }

    @Test
    void cualquieraPuedeBuscarYFiltrar() throws Exception {
        mvc.perform(get("/").param("ciudad", "Barranquilla").param("operacion", "ARRIENDO"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Apartamento con vista al mar")))
                .andExpect(content().string(not(containsString("Casa familiar con patio"))));
    }

    @Test
    void publicarRequiereIniciarSesion() throws Exception {
        mvc.perform(get("/publicar")).andExpect(status().is3xxRedirection());
    }

    @Test
    void registroCreaCuenta() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Ana").param("email", "ana@correo.co")
                        .param("celular", "3109876543").param("clave", "secreta123"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "admin@inmobiliaria.co")
    void usuarioPublicaInmuebleConFoto() throws Exception {
        var foto = new MockMultipartFile("nuevasFotos", "casa.jpg", "image/jpeg", new byte[]{1, 2, 3});
        mvc.perform(multipart("/publicar").file(foto).with(csrf())
                        .param("titulo", "Casa en Puerto Colombia").param("operacion", "VENTA")
                        .param("tipo", "CASA").param("precio", "350000000").param("ciudad", "Puerto Colombia"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/").param("texto", "puerto colombia"))
                .andExpect(content().string(containsString("Casa en Puerto Colombia")))
                .andExpect(content().string(containsString("/fotos/")));
    }

    @Test
    void mapasRecibenLasUbicaciones() throws Exception {
        mvc.perform(get("/estudiantes"))
                .andExpect(content().string(containsString("mapa-universidades")))
                .andExpect(content().string(containsString("11.019")));   // Uninorte
        mvc.perform(get("/").param("ciudad", "Barranquilla"))
                .andExpect(content().string(containsString("mapa-resultados")));
    }

    @Test
    @WithMockUser(username = "admin@inmobiliaria.co")
    void rechazaUbicacionFueraDeColombia() throws Exception {
        mvc.perform(multipart("/publicar").with(csrf())
                        .param("titulo", "Casa").param("operacion", "VENTA").param("tipo", "CASA")
                        .param("precio", "1").param("ciudad", "Cali")
                        .param("latitud", "40.4").param("longitud", "-3.7"))   // Madrid
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ubicación fuera de Colombia")));
    }

    @Test
    @WithMockUser(username = "admin@inmobiliaria.co")
    void rechazaArchivosQueNoSonFotos() throws Exception {
        var archivo = new MockMultipartFile("nuevasFotos", "virus.exe", "application/octet-stream", new byte[]{1});
        mvc.perform(multipart("/publicar").file(archivo).with(csrf())
                        .param("titulo", "X").param("operacion", "VENTA").param("tipo", "CASA")
                        .param("precio", "1").param("ciudad", "Cali"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Solo se permiten fotos")));
    }
}
