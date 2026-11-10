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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        "app.uploads.dir=target/test-uploads",
        // Las pruebas nunca envían correos reales, aunque exista un .env con credenciales.
        "spring.mail.username="})
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
                .andExpect(content().string(containsString("Cerca de ")));

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
                        .param("nombres", "Ana María").param("apellidos", "Pérez Rojas").param("email", "ana@correo.co")
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
                .andExpect(content().string(containsString("/contacto/companero/")));
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
                            .param("tipo", "BUSCO_CUARTO").param("universidad", uis.toString()).param("ciudad", "Bucaramanga")
                            .param("carrera", "Geología").param("semestre", "2").param("genero", "MUJER")
                            .param("presupuesto", "650000").param("descripcion", "Estudiante de primer año"))
                    .andExpect(redirectedUrl("/companeros"));
        }
        mvc.perform(get("/companeros").param("universidad", uis.toString()).param("ciudad", "Bucaramanga"))
                .andExpect(content().string(containsString("Geología")));
        mvc.perform(post("/companeros/nuevo").with(csrf())
                        .param("tipo", "BUSCO_CUARTO").param("universidad", uis.toString()).param("ciudad", "Bucaramanga")
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
        org.junit.jupiter.api.Assertions.assertTrue(distancia >= 70 && distancia <= 235,
                "El centro de la zona debe estar desplazado entre 80 y 220 m; está a " + distancia);
        org.junit.jupiter.api.Assertions.assertArrayEquals(zona, coordenadasDelMapa(html2, i.getId()),
                "La zona no puede cambiar entre visitas");
        org.junit.jupiter.api.Assertions.assertTrue(html1.contains("\"radio\":300"));
        org.junit.jupiter.api.Assertions.assertTrue(html1.contains("Ubicación aproximada"));
        // Minutos redondeados de 5 en 5 (8 -> 10) y distancia redondeada
        org.junit.jupiter.api.Assertions.assertTrue(html1.contains("Cerca de </span><strong>Universidad del Norte"));
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

    // ---------- Contactos, panel del administrador y registro de anunciantes ----------

    @Autowired com.juanamaris.inmobiliaria.repository.ConteoDiarioRepository conteos;

    private long contados(com.juanamaris.inmobiliaria.model.TipoEvento t) {
        return conteos.total(java.util.EnumSet.of(t), java.time.LocalDate.now().minusDays(2));
    }

    @Test
    void elClicEnWhatsappSeCuentaYRedirige() throws Exception {
        long antes = contados(com.juanamaris.inmobiliaria.model.TipoEvento.CONTACTO_WHATSAPP);
        mvc.perform(get("/contacto/inmueble/1/whatsapp"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Antes de pagar, visita el inmueble")));
        mvc.perform(get("/contacto/inmueble/1/whatsapp/abrir"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("https://wa.me/57")));
        mvc.perform(get("/contacto/inmueble/1/llamada")).andExpect(status().isNoContent());
        org.junit.jupiter.api.Assertions.assertEquals(antes + 1, contados(com.juanamaris.inmobiliaria.model.TipoEvento.CONTACTO_WHATSAPP));
        mvc.perform(get("/inmuebles/1")).andExpect(content().string(containsString("/contacto/inmueble/1/whatsapp")));
    }

    @Test
    void contactoDeCompaneroRequiereSesion() throws Exception {
        mvc.perform(get("/contacto/companero/1/whatsapp")).andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }

    @Test
    void panelSoloParaAdministradores() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/admin").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                .user("laura@ejemplo.co").roles("USER"))).andExpect(status().isForbidden());
        mvc.perform(get("/admin/anunciantes").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                .user("laura@ejemplo.co").roles("USER"))).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@inmobiliaria.co", roles = "ADMIN")
    void panelMuestraBusquedasSinResultadosYRegistro() throws Exception {
        mvc.perform(get("/").param("ciudad", "Leticia").param("operacion", "ARRIENDO"));
        mvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Panel del administrador")))
                .andExpect(content().string(containsString("Arriendo · Leticia")));
        mvc.perform(get("/admin/anunciantes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1000000000")));
    }

    @Test
    void losDatosDelRegistroNoSalenEnPaginasPublicas() throws Exception {
        for (String url : new String[]{"/", "/inmuebles/1", "/estudiantes", "/companeros"}) {
            mvc.perform(get(url)).andExpect(content().string(not(containsString("1000000000"))));
        }
    }

    @Test
    @WithMockUser(username = "laura@ejemplo.co")
    void paraPublicarHayQueLlenarElRegistroDeAnunciante() throws Exception {
        mvc.perform(get("/publicar")).andExpect(redirectedUrl("/mi-registro"));
        mvc.perform(post("/mi-registro").with(csrf())
                        .param("tipoDocumento", "CC").param("numeroDocumento", "1234567")
                        .param("direccionNotificaciones", "Dirección de prueba").param("ciudadNotificaciones", "Bucaramanga"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Debes declarar que los datos son verdaderos")));
        mvc.perform(post("/mi-registro").with(csrf())
                        .param("tipoDocumento", "CC").param("numeroDocumento", "1234567").param("declaro", "true")
                        .param("direccionNotificaciones", "Dirección de prueba").param("ciudadNotificaciones", "Bucaramanga"))
                .andExpect(redirectedUrl("/publicar"));
        mvc.perform(get("/publicar")).andExpect(status().isOk());
    }

    // ---------- Nombre público, reportes, bloqueos y avisos (consulta legal 003) ----------

    @Autowired com.juanamaris.inmobiliaria.repository.UsuarioRepository usuariosRepo;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;

    private com.juanamaris.inmobiliaria.model.Usuario crearUsuario(String email) {
        var u = new com.juanamaris.inmobiliaria.model.Usuario();
        u.setNombres("Prueba"); u.setApellidos("Usuario Test"); u.setNombre("Prueba Usuario Test");
        u.setEmail(email); u.setCelular("3110000000"); u.setPassword(encoder.encode("clave12345"));
        return usuariosRepo.save(u);
    }

    private com.juanamaris.inmobiliaria.model.Inmueble crearInmueble(com.juanamaris.inmobiliaria.model.Usuario dueno, String titulo) {
        var i = new com.juanamaris.inmobiliaria.model.Inmueble();
        i.setTitulo(titulo); i.setTipo(com.juanamaris.inmobiliaria.model.TipoInmueble.CASA);
        i.setOperacion(com.juanamaris.inmobiliaria.model.Operacion.ARRIENDO); i.setPrecio(1_000_000L);
        i.setCiudad("Montería"); i.setPropietario(dueno);
        return inmuebles.save(i);
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor como(String email) {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(email).roles("USER");
    }

    @Test
    void soloSeMuestraElNombreYLaInicialDelApellido() throws Exception {
        String detalle = mvc.perform(get("/inmuebles/1")).andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertTrue(detalle.contains("Administrador P."));
        org.junit.jupiter.api.Assertions.assertTrue(detalle.contains("Nombre informado por el anunciante"));
        org.junit.jupiter.api.Assertions.assertFalse(detalle.contains("Administrador Portal"));
        mvc.perform(get("/companeros")).andExpect(content().string(containsString("Laura M.")))
                .andExpect(content().string(not(containsString("Martínez"))));
        var admin = usuariosRepo.findByEmailIgnoreCase("admin@inmobiliaria.co").orElseThrow();
        mvc.perform(get("/anunciantes/" + admin.getId())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Administrador P.")))
                .andExpect(content().string(containsString("Casa familiar con patio")))
                .andExpect(content().string(not(containsString("Administrador Portal"))));
    }

    @Test
    void avisoAntiestafaEnDetalleYCompaneros() throws Exception {
        mvc.perform(get("/inmuebles/1")).andExpect(content().string(containsString("Antes de pagar, visita el inmueble")));
        mvc.perform(get("/companeros")).andExpect(content().string(containsString("Antes de pagar, visita el inmueble")));
        mvc.perform(get("/seguridad")).andExpect(status().isOk()).andExpect(content().string(containsString("tradición y libertad")));
        mvc.perform(get("/contacto/inmueble/1/whatsapp/abrir"))
                .andExpect(header().string("Location", not(containsString("Antes"))));
    }

    @Test
    void reportarRequiereSesionYNoSeMuestraEnElSitio() throws Exception {
        mvc.perform(get("/reportar").param("objetivo", "INMUEBLE").param("id", "1")).andExpect(status().is3xxRedirection());
        crearUsuario("reporta1@test.co");
        mvc.perform(post("/reportar").with(csrf()).with(como("reporta1@test.co"))
                        .param("objetivo", "INMUEBLE").param("id", "2").param("motivo", "ESTAFA")
                        .param("descripcion", "Me pidió consignar una reserva sin mostrarme la casa"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Si perdiste dinero, denuncia")));
        // Un segundo reporte del mismo usuario al mismo anuncio no se acepta
        mvc.perform(post("/reportar").with(csrf()).with(como("reporta1@test.co"))
                        .param("objetivo", "INMUEBLE").param("id", "2").param("motivo", "OTRO").param("descripcion", "otra vez"))
                .andExpect(content().string(containsString("Ya reportaste esta publicación")));
        // Con un solo reporte el anuncio sigue visible y el reporte no aparece en el sitio
        mvc.perform(get("/inmuebles/2")).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("consignar una reserva"))));
    }

    @Test
    void noSePuedeReportarLaPropiaPublicacion() throws Exception {
        mvc.perform(post("/reportar").with(csrf()).with(como("admin@inmobiliaria.co"))
                        .param("objetivo", "INMUEBLE").param("id", "1").param("motivo", "OTRO").param("descripcion", "prueba"))
                .andExpect(content().string(containsString("No puedes reportar tu propia publicación")));
    }

    @Test
    void tresCuentasConMotivoGraveOcultanElAnuncioMientrasSeRevisa() throws Exception {
        var dueno = crearUsuario("dueno.reportes@test.co");
        var i = crearInmueble(dueno, "Casa de prueba reportes");
        for (int k = 1; k <= 3; k++) {
            crearUsuario("rep" + k + ".grave@test.co");
            mvc.perform(get("/inmuebles/" + i.getId())).andExpect(status().isOk());
            mvc.perform(post("/reportar").with(csrf()).with(como("rep" + k + ".grave@test.co"))
                    .param("objetivo", "INMUEBLE").param("id", i.getId().toString())
                    .param("motivo", "INMUEBLE_FALSO").param("descripcion", "El inmueble no existe en esa zona"));
        }
        mvc.perform(get("/inmuebles/" + i.getId())).andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Este anuncio no está disponible en este momento.")))
                .andExpect(content().string(not(containsString("reportado"))));
        mvc.perform(get("/").param("ciudad", "Montería")).andExpect(content().string(not(containsString("Casa de prueba reportes"))));
        // El dueño ve que está en revisión y puede responder
        mvc.perform(get("/mis-inmuebles").with(como("dueno.reportes@test.co")))
                .andExpect(content().string(containsString("En revisión: no visible al público")));
        // El administrador ve los reportes y puede volver a mostrarlo
        mvc.perform(get("/admin/reportes/usuario/" + dueno.getId()).with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin@inmobiliaria.co").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El inmueble no existe en esa zona")));
        mvc.perform(post("/admin/publicaciones/restaurar").with(csrf()).with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin@inmobiliaria.co").roles("ADMIN"))
                        .param("objetivo", "INMUEBLE").param("id", i.getId().toString()).param("usuario", dueno.getId().toString()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/inmuebles/" + i.getId())).andExpect(status().isOk());
    }

    @Test
    void bloquearPorCorreoYCedula() throws Exception {
        var admin = org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin@inmobiliaria.co").roles("ADMIN");
        var malo = crearUsuario("estafador@test.co");
        var i = crearInmueble(malo, "Casa del estafador");
        mvc.perform(post("/mi-registro").with(csrf()).with(como("estafador@test.co"))
                .param("tipoDocumento", "CC").param("numeroDocumento", "98.765.432").param("declaro", "true")
                .param("direccionNotificaciones", "X").param("ciudadNotificaciones", "Cali"));
        mvc.perform(post("/admin/usuarios/" + malo.getId() + "/bloquear").with(csrf()).with(admin).param("motivo", "Estafa confirmada"))
                .andExpect(status().is3xxRedirection());
        // No puede ingresar
        mvc.perform(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin("/login")
                        .userParameter("email").user("estafador@test.co").password("clave12345"))
                .andExpect(redirectedUrl("/login?bloqueado"));
        // Si tenía sesión abierta, se le cierra
        mvc.perform(get("/mis-inmuebles").with(como("estafador@test.co"))).andExpect(redirectedUrl("/login?bloqueado"));
        // Sus anuncios dejan de verse
        mvc.perform(get("/inmuebles/" + i.getId())).andExpect(status().isNotFound());
        // No puede crear otra cuenta con el mismo correo
        mvc.perform(post("/registro").with(csrf()).param("nombres", "Otro").param("apellidos", "Nombre")
                        .param("email", "ESTAFADOR@test.co").param("celular", "3120000000").param("clave", "clave12345"))
                .andExpect(content().string(containsString("No es posible crear una cuenta con este correo")));
        // Con otra cuenta y la misma cédula (escrita distinto) queda bloqueado al registrarse como anunciante
        crearUsuario("otra.cuenta@test.co");
        mvc.perform(post("/mi-registro").with(csrf()).with(como("otra.cuenta@test.co"))
                        .param("tipoDocumento", "CC").param("numeroDocumento", "98765432").param("declaro", "true")
                        .param("direccionNotificaciones", "Y").param("ciudadNotificaciones", "Cali"))
                .andExpect(redirectedUrl("/login?bloqueado"));
        org.junit.jupiter.api.Assertions.assertTrue(usuariosRepo.findByEmailIgnoreCase("otra.cuenta@test.co").orElseThrow().isBloqueado());
        mvc.perform(get("/admin/bloqueos").with(admin)).andExpect(content().string(containsString("98765432")));
    }

    // ---------- Municipios, universidades por ciudad y barrios ----------

    @Autowired com.juanamaris.inmobiliaria.service.GeografiaService geografia;
    @Autowired com.juanamaris.inmobiliaria.service.UniversidadService universidadesService;
    @Autowired com.juanamaris.inmobiliaria.service.BarrioService barriosService;

    @Test
    void hayTodosLosMunicipiosYSeReconocenPorNombreComun() {
        org.junit.jupiter.api.Assertions.assertTrue(geografia.todos().size() > 1000, "Deben cargarse los municipios de DIVIPOLA");
        org.junit.jupiter.api.Assertions.assertEquals("Cali", geografia.buscar("santiago de cali").orElseThrow().nombre());
        org.junit.jupiter.api.Assertions.assertEquals("Cali", geografia.buscar("CALI").orElseThrow().nombre());
        org.junit.jupiter.api.Assertions.assertTrue(geografia.buscar("Montería").isPresent());
        org.junit.jupiter.api.Assertions.assertTrue(geografia.buscar("Ciudad Inventada").isEmpty());
    }

    @Test
    void universidadesDeOtrasCiudadesSeImportan() {
        var cali = universidadesService.de("Cali");
        org.junit.jupiter.api.Assertions.assertTrue(cali.stream().anyMatch(u -> u.getNombre().contains("Universidad del Valle")),
                "Debe estar la Universidad del Valle en Cali");
        org.junit.jupiter.api.Assertions.assertTrue(cali.stream().noneMatch(u -> u.getNombre().contains("Universidad del Norte")),
                "Las universidades de Cali no pueden incluir las de Barranquilla");
        org.junit.jupiter.api.Assertions.assertFalse(universidadesService.de("Barranquilla").isEmpty());
        // Las universidades ya catalogadas no se duplican
        long uninorte = universidadesService.de("Barranquilla").stream().filter(u -> u.getNombre().equals("Universidad del Norte")).count();
        org.junit.jupiter.api.Assertions.assertEquals(1, uninorte);
    }

    @Test
    void elFiltroMuestraSoloLasUniversidadesDeLaCiudadElegida() throws Exception {
        String html = mvc.perform(get("/").param("ciudad", "Cali")).andReturn().getResponse().getContentAsString();
        String menu = html.substring(html.indexOf("id=\"sel-universidad\""), html.indexOf("</select>", html.indexOf("id=\"sel-universidad\"")));
        org.junit.jupiter.api.Assertions.assertTrue(menu.contains("Univalle") || menu.contains("Universidad del Valle"));
        org.junit.jupiter.api.Assertions.assertFalse(menu.contains("Uninorte"), "No deben aparecer universidades de otra ciudad");
    }

    @Test
    @WithMockUser(username = "admin@inmobiliaria.co")
    void ciudadDebeSerDeLaListaYLaUniversidadDeLaMismaCiudad() throws Exception {
        Long uninorte = universidades.findBySigla("Uninorte").orElseThrow().getId();
        mvc.perform(multipart("/publicar").with(csrf())
                        .param("titulo", "Casa con ciudad inventada").param("operacion", "ARRIENDO").param("tipo", "CASA")
                        .param("precio", "900000").param("ciudad", "Ciudad Inventada"))
                .andExpect(content().string(containsString("Elige la ciudad de la lista")));
        mvc.perform(multipart("/publicar").with(csrf())
                        .param("titulo", "Casa en Cali con universidad de Barranquilla").param("operacion", "ARRIENDO").param("tipo", "CASA")
                        .param("precio", "900000").param("ciudad", "Cali").param("universidadCercana", uninorte.toString())
                        .param("minutosAPie", "10"))
                .andExpect(content().string(containsString("Elige una universidad de")));
    }

    @Test
    @WithMockUser(username = "admin@inmobiliaria.co")
    void losBarriosSeAprendenDeLasPublicaciones() throws Exception {
        mvc.perform(multipart("/publicar").with(csrf())
                        .param("titulo", "Apartamento en Chapinero Alto").param("operacion", "ARRIENDO").param("tipo", "APARTAMENTO")
                        .param("precio", "1800000").param("ciudad", "Bogotá").param("barrio", "  chapinero   ALTO "))
                .andExpect(status().is3xxRedirection());
        mvc.perform(multipart("/publicar").with(csrf())
                        .param("titulo", "Otro en Chapinero Alto").param("operacion", "VENTA").param("tipo", "CASA")
                        .param("precio", "300000000").param("ciudad", "Bogotá").param("barrio", "Chapinero Alto"))
                .andExpect(status().is3xxRedirection());
        var barrios = barriosService.mapaPorCiudad().get("Bogotá");
        org.junit.jupiter.api.Assertions.assertNotNull(barrios);
        org.junit.jupiter.api.Assertions.assertEquals(1, barrios.stream().filter(b -> b.equals("Chapinero Alto")).count(),
                "Un mismo barrio no se repite aunque cambie la escritura");
        mvc.perform(get("/")).andExpect(content().string(containsString("Chapinero Alto")));
    }

    // ---------- Dirección privada del inmueble ----------

    @Test
    @WithMockUser(username = "admin@inmobiliaria.co")
    void laDireccionEsPrivadaYSoloLaVeElDueno() throws Exception {
        mvc.perform(multipart("/publicar").with(csrf())
                        .param("titulo", "Casa con dirección privada zz").param("operacion", "ARRIENDO").param("tipo", "CASA")
                        .param("precio", "1200000").param("ciudad", "Barranquilla")
                        .param("direccionPrivada", "Calle 98 # 52-14 DIRPRIV"))
                .andExpect(status().is3xxRedirection());
        var i = inmuebles.findAll().stream().filter(x -> "Casa con dirección privada zz".equals(x.getTitulo())).findFirst().orElseThrow();
        // Ninguna página pública la muestra
        mvc.perform(get("/inmuebles/" + i.getId())).andExpect(content().string(not(containsString("DIRPRIV"))));
        mvc.perform(get("/")).andExpect(content().string(not(containsString("DIRPRIV"))));
        mvc.perform(get("/companeros")).andExpect(content().string(not(containsString("DIRPRIV"))));
        // Sí aparece para el dueño al editar
        mvc.perform(get("/inmuebles/" + i.getId() + "/editar")).andExpect(content().string(containsString("DIRPRIV")));
    }

    // ---------- Zonas antiguas se ajustan al nuevo tamaño ----------

    @Autowired com.juanamaris.inmobiliaria.service.UbicacionPrivadaService ubicacionPrivada;
    @Autowired com.juanamaris.inmobiliaria.config.MigracionZonasPublicas migracionZonas;

    @Test
    void lasZonasAntiguasMuyAlejadasSeRecalculanAlArrancar() throws Exception {
        var dueno = crearUsuario("zona.vieja@test.co");
        var i = crearInmueble(dueno, "Casa con zona antigua");
        i.setLatitud(10.98); i.setLongitud(-74.80);
        i.setZonaPublica(10.98 + 290 / 111_320d, -74.80);   // desplazada 290 m, como con la configuración anterior
        inmuebles.save(i);
        org.junit.jupiter.api.Assertions.assertFalse(ubicacionPrivada.zonaValida(i));
        migracionZonas.run();
        var actualizado = inmuebles.findById(i.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(ubicacionPrivada.zonaValida(actualizado),
                "Tras la migración el punto real debe quedar dentro de la zona de 300 m");
    }

    @Test
    void elMapaDeEstudiantesMuestraInmueblesCercaDeUniversidades() throws Exception {
        String html = mvc.perform(get("/estudiantes")).andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertTrue(html.contains("Habitaci\\u00F3n amoblada a pasos de Uninorte")
                || html.contains("Habitación amoblada a pasos de Uninorte"), "Debe incluir inmuebles cerca de universidades");
        org.junit.jupiter.api.Assertions.assertFalse(html.contains("Casa familiar con patio"),
                "Los inmuebles sin universidad cercana no van en este mapa");
        org.junit.jupiter.api.Assertions.assertTrue(html.contains("\"radio\":300"));
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    void elDetalleMuestraLaGaleriaConUnaYConVariasFotos() throws Exception {
        var inm = inmuebles.findAll().stream().filter(x -> x.isActivo() && !x.isEnRevision()).findFirst().orElseThrow();
        inm.getFotos().clear();
        inm.getFotos().add("prueba-1.jpg");
        inmuebles.saveAndFlush(inm);
        String una = mvc.perform(get("/inmuebles/" + inm.getId())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertTrue(una.contains("galeria n1") && una.contains("galeria-fondo"));
        for (int k = 2; k <= 7; k++) inm.getFotos().add("prueba-" + k + ".jpg");
        inmuebles.saveAndFlush(inm);
        String varias = mvc.perform(get("/inmuebles/" + inm.getId())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertTrue(varias.contains("galeria n5") && varias.contains("Ver las 7 fotos"));
    }

    @Autowired com.juanamaris.inmobiliaria.service.RecuperacionService recuperacion;
    @Autowired com.juanamaris.inmobiliaria.repository.TokenRecuperacionRepository tokensRecuperacion;

    private com.juanamaris.inmobiliaria.model.Usuario cuentaParaRecuperar(String email) {
        var u = new com.juanamaris.inmobiliaria.model.Usuario();
        u.setNombres("Rosa"); u.setApellidos("Prueba Recuperar"); u.setNombre("Rosa Prueba Recuperar");
        u.setEmail(email); u.setCelular("3001112233"); u.setRol("USER");
        u.setPassword(encoder.encode("vieja-clave-1"));
        return usuariosRepo.save(u);
    }

    @Test
    void recuperarContrasenaNoRevelaSiElCorreoExiste() throws Exception {
        cuentaParaRecuperar("existe.recuperar@test.co");
        for (String correo : new String[] {"existe.recuperar@test.co", "no-existe-nunca@test.co"}) {
            mvc.perform(post("/recuperar").with(csrf()).param("email", correo))
                    .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/recuperar"))
                    .andExpect(flash().attribute("enviado", true));
        }
        mvc.perform(get("/recuperar")).andExpect(status().isOk()).andExpect(content().string(containsString("Enviar enlace")));
        mvc.perform(get("/login")).andExpect(content().string(containsString("¿Olvidaste tu contraseña?")));
    }

    @Test
    void elEnlaceCambiaLaContrasenaUnaSolaVezYSoloSeGuardaElHash() throws Exception {
        var u = cuentaParaRecuperar("cambia.recuperar@test.co");
        String codigo = recuperacion.solicitar("Cambia.Recuperar@test.co").orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(tokensRecuperacion.findAll().stream().noneMatch(t -> t.getHash().equals(codigo)),
                "El código no se guarda tal cual");
        mvc.perform(get("/restablecer").param("codigo", codigo)).andExpect(status().isOk())
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(content().string(containsString("Guardar contraseña")));
        // Contraseñas distintas: no cambia
        mvc.perform(post("/restablecer").with(csrf()).param("codigo", codigo).param("clave", "nueva-clave-1").param("clave2", "otra-clave-2"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("no coinciden")));
        mvc.perform(post("/restablecer").with(csrf()).param("codigo", codigo).param("clave", "nueva-clave-1").param("clave2", "nueva-clave-1"))
                .andExpect(redirectedUrl("/login"));
        var actualizado = usuariosRepo.findById(u.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(encoder.matches("nueva-clave-1", actualizado.getPassword()));
        org.junit.jupiter.api.Assertions.assertNotNull(actualizado.getCredencialesCambiadasEn());
        // Segunda vez con el mismo enlace: ya no sirve
        mvc.perform(post("/restablecer").with(csrf()).param("codigo", codigo).param("clave", "tercera-clave-3").param("clave2", "tercera-clave-3"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("ya no es válido")));
        org.junit.jupiter.api.Assertions.assertTrue(encoder.matches("nueva-clave-1", usuariosRepo.findById(u.getId()).orElseThrow().getPassword()));
        // Se puede ingresar con la nueva
        mvc.perform(formLogin("/login").user("email", "cambia.recuperar@test.co").password("nueva-clave-1"))
                .andExpect(redirectedUrl("/mis-inmuebles"));
    }

    @Test
    void elEnlaceVenceYHayLimitePorHora() throws Exception {
        var cuenta = cuentaParaRecuperar("limite.recuperar@test.co");
        String primero = recuperacion.solicitar("limite.recuperar@test.co").orElseThrow();
        String segundo = recuperacion.solicitar("limite.recuperar@test.co").orElseThrow();
        org.junit.jupiter.api.Assertions.assertFalse(recuperacion.valido(primero), "Solo vale el enlace más reciente");
        org.junit.jupiter.api.Assertions.assertTrue(recuperacion.valido(segundo));
        String tercero = recuperacion.solicitar("limite.recuperar@test.co").orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(recuperacion.solicitar("limite.recuperar@test.co").isEmpty(), "Máximo 3 por hora");
        // Vencido: deja de servir
        var t = tokensRecuperacion.findAll().stream().filter(x -> x.getUsadoEn() == null
                && x.getUsuario().getId().equals(cuenta.getId())).findFirst().orElseThrow();
        t.setVenceEn(java.time.LocalDateTime.now().minusYears(1));
        tokensRecuperacion.save(t);
        org.junit.jupiter.api.Assertions.assertFalse(recuperacion.valido(tercero), "Un enlace vencido no sirve");
        mvc.perform(get("/restablecer").param("codigo", "codigo-inventado-que-no-existe-123456"))
                .andExpect(content().string(containsString("ya no es válido")));
    }

    @Test
    void cambiarLaContrasenaCierraLasSesionesAbiertas() throws Exception {
        var u = cuentaParaRecuperar("sesiones.recuperar@test.co");
        var login = mvc.perform(formLogin("/login").user("email", "sesiones.recuperar@test.co").password("vieja-clave-1"))
                .andExpect(redirectedUrl("/mis-inmuebles")).andReturn();
        var sesion = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(get("/mis-inmuebles").session(sesion)).andExpect(status().isOk());
        String codigo = recuperacion.solicitar("sesiones.recuperar@test.co").orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(recuperacion.restablecer(codigo, "clave-nueva-99"));
        mvc.perform(get("/mis-inmuebles").session(sesion)).andExpect(redirectedUrl("/login?sesion"));
    }
}
