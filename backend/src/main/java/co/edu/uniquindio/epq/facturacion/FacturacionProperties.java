package co.edu.uniquindio.epq.facturacion;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parámetros configurables del módulo de facturación (F-01).
 *
 * @param ejecucionAutomatica     habilita la tarea automática del primer día hábil (SWR-01)
 * @param cron                    expresión cron con la que se evalúa si corresponde facturar
 * @param diasParaVencimiento     días calendario entre la emisión y el vencimiento de la factura
 * @param tiempoMaximoLoteMinutos tiempo máximo esperado para un lote (SWR-02)
 */
@ConfigurationProperties(prefix = "app.facturacion")
public record FacturacionProperties(
        @DefaultValue("true") boolean ejecucionAutomatica,
        @DefaultValue("0 0 1 * * *") String cron,
        @DefaultValue("15") int diasParaVencimiento,
        @DefaultValue("60") int tiempoMaximoLoteMinutos) {
}
