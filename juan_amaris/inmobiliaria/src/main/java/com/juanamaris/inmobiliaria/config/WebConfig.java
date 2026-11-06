package com.juanamaris.inmobiliaria.config;

import com.juanamaris.inmobiliaria.service.AlmacenFotosService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Sirve las fotos subidas en /fotos/{archivo}. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AlmacenFotosService fotos;
    private final com.juanamaris.inmobiliaria.service.ActividadService actividad;
    private final com.juanamaris.inmobiliaria.repository.UsuarioRepository usuarios;

    public WebConfig(AlmacenFotosService fotos, com.juanamaris.inmobiliaria.service.ActividadService actividad,
                     com.juanamaris.inmobiliaria.repository.UsuarioRepository usuarios) {
        this.usuarios = usuarios;
        this.fotos = fotos;
        this.actividad = actividad;
    }

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(new ContadorVisitas(actividad));
        registry.addInterceptor(new SesionBloqueada(usuarios));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/fotos/**")
                .addResourceLocations(fotos.getCarpeta().toUri().toString());
    }
}
