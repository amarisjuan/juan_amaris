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
