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
        var foto = new MockMultipartFile("nuevasFotos", "casa.jpg", "image/jpeg", jpegConGps());
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

    // ---------- Reglas de la vivienda ----------

    @Test
    void filtroParaHombreOcultaLasDeSoloMujeres() throws Exception {
        mvc.perform(get("/").param("ciudad", "Bogotá").param("para", "HOMBRE"))
                .andExpect(content().string(not(containsString("Habitación para estudiante con servicios incluidos"))));
        mvc.perform(get("/").param("ciudad", "Bogotá").param("para", "MUJER"))
                .andExpect(content().string(containsString("Habitación para estudiante con servicios incluidos")))
                .andExpect(content().string(containsString("Solo mujeres")));
    }

    @Test
    void filtroMascotas() throws Exception {
        mvc.perform(get("/").param("mascotas", "true"))
                .andExpect(content().string(containsString("Apartaestudio amoblado cerca a universidades")))
                .andExpect(content().string(not(containsString("Casa familiar con patio"))));
    }

    @Test
    @WithMockUser(username = "admin@inmobiliaria.co")
    void publicarConReglas() throws Exception {
        mvc.perform(multipart("/publicar").with(csrf())
                        .param("titulo", "Cuarto con reglas claras").param("operacion", "ARRIENDO")
                        .param("tipo", "HABITACION").param("precio", "600000").param("ciudad", "Cali")
                        .param("convivencia", "SOLO_HOMBRES").param("permiteMascotas", "true")
                        .param("horarioLlegada", "Sin restricción").param("otrasReglas", "Nada de fiestas"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/").param("texto", "reglas claras"))
                .andExpect(content().string(containsString("Solo hombres")));
    }

    // ---------- Compañeros de cuarto ----------

    @Test
    void tableroEsPublicoPeroElContactoRequiereSesion() throws Exception {
        mvc.perform(get("/companeros"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ingeniería Industrial")))
                .andExpect(content().string(containsString("Inicia sesión para contactar")))
                .andExpect(content().string(not(containsString("wa.me/573007654321"))));
    }

    @Test
    @WithMockUser(username = "admin@inmobiliaria.co")
    void conSesionSeVeElWhatsapp() throws Exception {
        mvc.perform(get("/companeros"))
                .andExpect(content().string(containsString("wa.me/573007654321")));
    }

    @Test
    void filtroSoyHombreOcultaQuienBuscaSoloMujeres() throws Exception {
        mvc.perform(get("/companeros").param("soy", "HOMBRE"))
                .andExpect(content().string(not(containsString("Ingeniería Industrial"))))
                .andExpect(content().string(containsString("Economía")));
    }

    @Test
    void publicarCompaneroRequiereSesion() throws Exception {
        mvc.perform(get("/companeros/nuevo")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "laura@ejemplo.co")
    void publicarCompaneroYLimiteDePublicaciones() throws Exception {
        Long uis = universidades.findBySigla("UIS").orElseThrow().getId();
        for (int n = 0; n < 2; n++) {   // Laura ya tiene 1 de ejemplo -> llega a 3
            mvc.perform(post("/companeros/nuevo").with(csrf())
                            .param("tipo", "BUSCO_CUARTO").param("universidad", uis.toString())
                            .param("carrera", "Geología").param("semestre", "2").param("genero", "MUJER")
                            .param("presupuesto", "650000").param("descripcion", "Estudiante de primer año"))
                    .andExpect(redirectedUrl("/companeros"));
        }
        mvc.perform(get("/companeros").param("universidad", uis.toString()))
                .andExpect(content().string(containsString("Geología")));
        mvc.perform(post("/companeros/nuevo").with(csrf())
                        .param("tipo", "BUSCO_CUARTO").param("universidad", uis.toString())
                        .param("carrera", "Geología").param("genero", "MUJER")
                        .param("presupuesto", "650000").param("descripcion", "Una más"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("máximo 3 publicaciones")));
    }

    // ---------- Privacidad de la ubicación (consulta legal 001) ----------

    @Autowired com.juanamaris.inmobiliaria.repository.InmuebleRepository inmuebles;

    /** JPEG real de 8x8 con un segmento APP1 de EXIF que simula coordenadas GPS. */
    static byte[] jpegConGps() throws Exception {
        var img = new java.awt.image.BufferedImage(8, 8, java.awt.image.BufferedImage.TYPE_INT_RGB);
        var buf = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "jpg", buf);
        byte[] jpg = buf.toByteArray();
        byte[] exif = "Exif\0\0GPSLatitude=11.0150;GPSLongitude=-74.8460".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        var out = new java.io.ByteArrayOutputStream();
        out.write(jpg, 0, 2);                                   // SOI
        out.write(0xFF); out.write(0xE1);                       // APP1
        out.write((exif.length + 2) >> 8); out.write((exif.length + 2) & 0xFF);
        out.write(exif);
        out.write(jpg, 2, jpg.length - 2);
        return out.toByteArray();
    }

    static double metros(double lat1, double lng1, double lat2, double lng2) {
        double r = 6_371_000, dLat = Math.toRadians(lat2 - lat1), dLng = Math.toRadians(lng2 - lng1);
        double h = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLng / 2), 2);
        return 2 * r * Math.asin(Math.sqrt(h));
    }

    private double[] coordenadasDelMapa(String html, long id) {
        var m = java.util.regex.Pattern.compile("\"lat\":([-0-9.]+),\"lng\":([-0-9.]+),\"url\":\"\\\\?/inmuebles\\\\?/" + id + "\"").matcher(html);
        if (!m.find()) {
            int k = html.indexOf("const casa");
            org.junit.jupiter.api.Assertions.fail("El mapa debe recibir la zona del inmueble. Encontrado: "
                    + (k < 0 ? "(sin mapa)" : html.substring(k, Math.min(html.length(), k + 300))));
        }
        return new double[]{Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2))};
    }

    @Test
    void elMapaRecibeSoloUnaZonaAproximadaYEstable() throws Exception {
        var i = inmuebles.findAll().stream().filter(x -> x.getTitulo().equals("Habitación amoblada a pasos de Uninorte"))
                .findFirst().orElseThrow();
        String html1 = mvc.perform(get("/inmuebles/" + i.getId())).andReturn().getResponse().getContentAsString();
        String html2 = mvc.perform(get("/inmuebles/" + i.getId())).andReturn().getResponse().getContentAsString();
        double[] zona = coordenadasDelMapa(html1, i.getId());
        double distancia = metros(zona[0], zona[1], i.getLatitud(), i.getLongitud());
        org.junit.jupiter.api.Assertions.assertTrue(distancia >= 90 && distancia <= 310,
                "El centro de la zona debe estar desplazado entre 100 y 300 m; está a " + distancia);
        org.junit.jupiter.api.Assertions.assertArrayEquals(zona, coordenadasDelMapa(html2, i.getId()),
                "La zona no puede cambiar entre visitas");
        org.junit.jupiter.api.Assertions.assertTrue(html1.contains("\"radio\":400"));
        org.junit.jupiter.api.Assertions.assertTrue(html1.contains("Ubicación aproximada"));
        // Minutos redondeados de 5 en 5 (8 -> 10) y distancia redondeada
        org.junit.jupiter.api.Assertions.assertTrue(html1.contains("Unos 10 minutos a pie de"));
        org.junit.jupiter.api.Assertions.assertTrue(html1.contains("en línea recta de Universidad del Norte"));
    }

    @Test
    @WithMockUser(username = "admin@inmobiliaria.co")
    void rechazaDireccionesExactasEnLosTextos() throws Exception {
        mvc.perform(multipart("/publicar").with(csrf())
                        .param("titulo", "Apartamento").param("operacion", "ARRIENDO").param("tipo", "APARTAMENTO")
                        .param("precio", "1000000").param("ciudad", "Barranquilla")
                        .param("descripcion", "Queda en la Calle 45 # 23-10, piso 2."))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no escribas la dirección exacta")));
        mvc.perform(multipart("/publicar").with(csrf())
                        .param("titulo", "Apartamento con referencia").param("operacion", "ARRIENDO").param("tipo", "APARTAMENTO")
                        .param("precio", "1000000").param("ciudad", "Barranquilla")
                        .param("descripcion", "A dos cuadras de la Calle 72, cerca al centro comercial."))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "laura@ejemplo.co")
    void compañeroNoPuedePublicarDireccion() throws Exception {
        Long uni = universidades.findBySigla("UIS").orElseThrow().getId();
        mvc.perform(post("/companeros/nuevo").with(csrf())
                        .param("tipo", "TENGO_CUARTO").param("universidad", uni.toString()).param("carrera", "Derecho")
                        .param("genero", "MUJER").param("presupuesto", "600000").param("barrio", "Cra 27 No. 9-60")
                        .param("descripcion", "Tengo cuarto libre"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no escribas la dirección exacta")));
    }

    @Test
    void detectorDeDirecciones() {
        var d = (java.util.function.Function<String, Boolean>) t -> com.juanamaris.inmobiliaria.service.DetectorDirecciones.buscar(t).isPresent();
        for (String si : new String[]{"Calle 45 # 23-10", "cra 54 No. 68-80", "Kr 7 #45-10", "Diagonal 12B bis N° 3-20",
                "queda en el # 23-10", "apto 302", "Torre 2"}) {
            org.junit.jupiter.api.Assertions.assertTrue(d.apply(si), "Debería detectar: " + si);
        }
        for (String no : new String[]{"A dos cuadras de la Calle 26", "precio 1.500.000", "3 habitaciones y 2 baños",
                "Cerca a la Avenida Boyacá", "a 10 minutos de Uninorte"}) {
            org.junit.jupiter.api.Assertions.assertFalse(d.apply(no), "No debería detectar: " + no);
        }
    }

    @Test
    void lasFotosSeGuardanSinMetadatosGps() throws Exception {
        byte[] original = jpegConGps();
        org.junit.jupiter.api.Assertions.assertTrue(new String(original, java.nio.charset.StandardCharsets.ISO_8859_1).contains("GPSLatitude"));
        byte[] limpia = com.juanamaris.inmobiliaria.service.LimpiadorMetadatos.limpiar(original, "image/jpeg");
        String texto = new String(limpia, java.nio.charset.StandardCharsets.ISO_8859_1);
        org.junit.jupiter.api.Assertions.assertFalse(texto.contains("GPS"));
        org.junit.jupiter.api.Assertions.assertFalse(texto.contains("Exif"));
        org.junit.jupiter.api.Assertions.assertNotNull(javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(limpia)),
                "La foto limpia debe seguir siendo una imagen válida");
    }
}
