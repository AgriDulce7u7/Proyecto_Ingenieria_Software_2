package co.edu.uniquindio.epq.comun.excepcion;

import org.springframework.http.HttpStatus;

/**
 * Un sistema externo (ERP, pasarela de pagos, RNEC) no respondió (HTTP 503).
 */
public class ServicioExternoNoDisponibleException extends ExcepcionAplicacion {

    public ServicioExternoNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public ServicioExternoNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    @Override
    public HttpStatus estadoHttp() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }

    @Override
    public String titulo() {
        return "Servicio externo no disponible";
    }
}
