package co.edu.uniquindio.epq.comun.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expone un {@link Clock} como dependencia (DIP): ningún servicio llama directamente a
 * {@code LocalDateTime.now()}, lo que permite probar reglas de plazos con un reloj fijo.
 */
@Configuration
public class TiempoConfig {

    @Bean
    public Clock clock(@Value("${app.zona-horaria:America/Bogota}") String zonaHoraria) {
        return Clock.system(ZoneId.of(zonaHoraria));
    }
}
