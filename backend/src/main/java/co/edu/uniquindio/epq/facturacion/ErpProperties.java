package co.edu.uniquindio.epq.facturacion;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración de la integración con el ERP financiero (IS-01, SWR-04).
 *
 * @param modo                   {@code simulado} (por defecto) o {@code rest}
 * @param url                    URL base del ERP cuando el modo es {@code rest}
 * @param maxIntentos            intentos por factura antes de marcarla con error
 * @param esperaEntreIntentosMs  pausa entre reintentos
 * @param simulado               parámetros del adaptador simulado
 */
@ConfigurationProperties(prefix = "app.erp")
public record ErpProperties(
        @DefaultValue("simulado") String modo,
        @DefaultValue("http://localhost:9090/api/erp") String url,
        @DefaultValue("3") int maxIntentos,
        @DefaultValue("200") long esperaEntreIntentosMs,
        @DefaultValue Simulado simulado) {

    /**
     * @param tasaFallo probabilidad (0.0 - 1.0) de que el ERP simulado rechace una factura
     */
    public record Simulado(@DefaultValue("0.0") double tasaFallo) {
    }
}
