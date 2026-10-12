package co.edu.uniquindio.epq.pagos.pasarela;

/**
 * Confirmación o rechazo de la transacción (DS-03 paso 8).
 */
public record RespuestaPasarela(boolean aprobada, String codigoTransaccion, String codigoRespuesta, String mensaje) {

    public static RespuestaPasarela aprobada(String codigoTransaccion, String mensaje) {
        return new RespuestaPasarela(true, codigoTransaccion, "00", mensaje);
    }

    public static RespuestaPasarela rechazada(String codigoRespuesta, String mensaje) {
        return new RespuestaPasarela(false, null, codigoRespuesta, mensaje);
    }
}
