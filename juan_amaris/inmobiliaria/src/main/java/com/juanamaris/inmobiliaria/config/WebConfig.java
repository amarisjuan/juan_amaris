package com.juanamaris.inmobiliaria.config;

import com.juanamaris.inmobiliaria.service.AlmacenFotosService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Sirve las fotos subidas en /fotos/{archivo}. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AlmacenFotosService fotos;

    public WebConfig(AlmacenFotosService fotos) { this.fotos = fotos; }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/fotos/**")
                .addResourceLocations(fotos.getCarpeta().toUri().toString());
    }
}
