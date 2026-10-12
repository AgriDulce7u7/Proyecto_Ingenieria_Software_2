package co.edu.uniquindio.epq.seguridad.servicio;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import co.edu.uniquindio.epq.comun.excepcion.ExcepcionAplicacion;

/**
 * CU-05 2a: la cuenta quedó bloqueada temporalmente por intentos fallidos (HTTP 423).
 */
public final class CuentaBloqueadaException extends ExcepcionAplicacion {

    public CuentaBloqueadaException(LocalDateTime bloqueadoHasta) {
        super("La cuenta está bloqueada temporalmente por intentos fallidos. Intente de nuevo más tarde.");
        agregarPropiedad("bloqueadoHasta", bloqueadoHasta);
    }

    @Override
    public HttpStatus estadoHttp() {
        return HttpStatus.LOCKED;
    }

    @Override
    public String titulo() {
        return "Cuenta bloqueada";
    }
}
