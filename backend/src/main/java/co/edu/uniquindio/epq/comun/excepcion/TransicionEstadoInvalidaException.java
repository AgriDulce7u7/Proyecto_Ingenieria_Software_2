package co.edu.uniquindio.epq.comun.excepcion;

/**
 * Se lanza cuando se intenta un cambio de estado no permitido por la máquina de estados (HTTP 409).
 */
public class TransicionEstadoInvalidaException extends ReglaNegocioException {

    public TransicionEstadoInvalidaException(String estadoActual, String estadoDestino) {
        super("No es posible pasar del estado '%s' al estado '%s'".formatted(estadoActual, estadoDestino));
    }
}
