package co.edu.uniquindio.epq.comun.excepcion;

import org.springframework.http.HttpStatus;

/**
 * El recurso que se intenta crear ya existe (HTTP 409).
 */
public class RecursoDuplicadoException extends ExcepcionAplicacion {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }

    /** Permite incluir datos del recurso existente para que el frontend ofrezca consultarlo. */
    public RecursoDuplicadoException conDato(String nombre, Object valor) {
        agregarPropiedad(nombre, valor);
        return this;
    }

    @Override
    public HttpStatus estadoHttp() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String titulo() {
        return "Recurso duplicado";
    }
}
