package co.edu.uniquindio.epq.pqr;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parámetros configurables del módulo PQR.
 *
 * @param horasAlertaVencimiento horas antes del vencimiento en que se alerta al gestor (SWR-07)
 * @param monitoreoCron          expresión cron del monitoreo de plazos
 */
@ConfigurationProperties(prefix = "app.pqr")
public record PqrProperties(
        @DefaultValue("48") int horasAlertaVencimiento,
        @DefaultValue("0 */15 * * * *") String monitoreoCron) {
}
