package co.edu.uniquindio.epq.comun.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Orígenes autorizados para consumir la API desde el frontend React.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        @DefaultValue({"http://localhost:5173", "http://localhost:3000"}) List<String> allowedOrigins) {
}
