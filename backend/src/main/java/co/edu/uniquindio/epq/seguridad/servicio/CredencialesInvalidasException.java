package co.edu.uniquindio.epq.seguridad.servicio;

import org.springframework.http.HttpStatus;

import co.edu.uniquindio.epq.comun.excepcion.ExcepcionAplicacion;

/**
 * Usuario o contraseña incorrectos (HTTP 401). El mensaje es genérico a propósito: no revela si el
 * usuario existe.
 */
public final class CredencialesInvalidasException extends ExcepcionAplicacion {

    public CredencialesInvalidasException(Integer intentosRestantes) {
        super("Usuario o contraseña incorrectos");
        if (intentosRestantes != null) {
            agregarPropiedad("intentosRestantes", intentosRestantes);
        }
    }

    @Override
    public HttpStatus estadoHttp() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String titulo() {
        return "Credenciales inválidas";
    }
}
