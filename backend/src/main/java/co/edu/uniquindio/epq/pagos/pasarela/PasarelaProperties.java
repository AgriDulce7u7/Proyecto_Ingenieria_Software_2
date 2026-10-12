package co.edu.uniquindio.epq.pagos.pasarela;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param modo            {@code simulada} (por defecto) o {@code rest}
 * @param url             URL base de la pasarela (modo rest)
 * @param timeoutSegundos CO-02: tiempo máximo de espera por intento (30 s)
 * @param maxIntentos     CO-02: intentos antes de reportar error (2)
 */
@ConfigurationProperties(prefix = "app.pasarela")
public record PasarelaProperties(
        @DefaultValue("simulada") String modo,
        @DefaultValue("http://localhost:9092/api/pasarela") String url,
        @DefaultValue("30") int timeoutSegundos,
        @DefaultValue("2") int maxIntentos) {
}
