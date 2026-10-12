package co.edu.uniquindio.epq.comercial.rnec;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param modo                {@code simulado} (por defecto) o {@code rest}
 * @param url                 URL base del servicio RNEC (modo rest)
 * @param timeoutSegundos     tiempo máximo de espera
 * @param simuladoDisponible  en modo simulado, {@code false} simula una caída total del servicio
 */
@ConfigurationProperties(prefix = "app.rnec")
public record RnecProperties(
        @DefaultValue("simulado") String modo,
        @DefaultValue("http://localhost:9091/api/rnec") String url,
        @DefaultValue("5") int timeoutSegundos,
        @DefaultValue("true") boolean simuladoDisponible) {
}
