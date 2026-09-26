package co.edu.uniquindio.epq.comun.excepcion;

/**
 * Se lanza cuando un recurso solicitado no existe (HTTP 404).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Object identificador) {
        super("%s '%s' no encontrado".formatted(recurso, identificador));
    }
}
