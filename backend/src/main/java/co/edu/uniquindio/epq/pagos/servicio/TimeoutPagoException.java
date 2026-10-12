package co.edu.uniquindio.epq.pagos.servicio;

import org.springframework.http.HttpStatus;

import co.edu.uniquindio.epq.comun.excepcion.ExcepcionAplicacion;

/**
 * CO-02: la pasarela no respondió tras los reintentos. La factura no se modifica (RN-05). HTTP 504.
 */
public final class TimeoutPagoException extends ExcepcionAplicacion {

    public TimeoutPagoException(int intentos) {
        super("La pasarela de pagos no respondió después de %d intentos. Su factura no fue modificada; intente más tarde."
                .formatted(intentos));
        agregarPropiedad("intentos", intentos);
    }

    @Override
    public HttpStatus estadoHttp() {
        return HttpStatus.GATEWAY_TIMEOUT;
    }

    @Override
    public String titulo() {
        return "Pasarela sin respuesta";
    }
}
