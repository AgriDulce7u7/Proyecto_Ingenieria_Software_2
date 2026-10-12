package co.edu.uniquindio.epq.pagos.pasarela;

import java.math.BigDecimal;

/**
 * @param referencia identificador único del intento; se reutiliza en los reintentos para que la
 *                   pasarela no cobre dos veces (idempotencia)
 */
public record SolicitudCobro(String referencia, BigDecimal monto, DatosTarjeta tarjeta, String descripcion) {
}
