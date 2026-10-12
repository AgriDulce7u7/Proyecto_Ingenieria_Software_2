package co.edu.uniquindio.epq.pagos.servicio;

import org.springframework.http.HttpStatus;

import co.edu.uniquindio.epq.comun.excepcion.ExcepcionAplicacion;

/**
 * CO-02: la pasarela rechazó la transacción. La factura sigue pendiente y no se genera comprobante. HTTP 402.
 */
public final class PagoRechazadoException extends ExcepcionAplicacion {

    public PagoRechazadoException(String codigoRechazo, String mensaje) {
        super("La pasarela rechazó el pago: " + mensaje + ". Su factura continúa pendiente.");
        agregarPropiedad("codigoRechazo", codigoRechazo);
    }

    @Override
    public HttpStatus estadoHttp() {
        return HttpStatus.PAYMENT_REQUIRED;
    }

    @Override
    public String titulo() {
        return "Pago rechazado";
    }
}
