package co.edu.uniquindio.epq.comun.excepcion;

/**
 * Se lanza cuando una operación viola una regla de negocio (HTTP 422).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
