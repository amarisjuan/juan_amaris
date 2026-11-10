package com.juanamaris.inmobiliaria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
@org.springframework.scheduling.annotation.EnableScheduling
public class RelojConfig {
    /** Los conteos diarios se cortan a medianoche en hora de Colombia. */
    @Bean
    public Clock reloj() { return Clock.system(ZoneId.of("America/Bogota")); }
}
