package co.edu.uniquindio.epq.pagos.servicio;

import co.edu.uniquindio.epq.pagos.web.dto.ComprobantePagoResponse;
import co.edu.uniquindio.epq.pagos.web.dto.PagoEnLineaRequest;
import co.edu.uniquindio.epq.pagos.web.dto.RegistrarPagoPresencialRequest;

/**
 * F-05 / CU-04 Registrar pago.
 */
public interface RegistroPagoService {

    /** SyR-05a: pago registrado por el funcionario (canal PRESENCIAL o BANCO). */
    ComprobantePagoResponse registrarPresencial(RegistrarPagoPresencialRequest solicitud);

    /** SyR-05b / DS-03: pago en línea del ciudadano autenticado a través de la pasarela (IS-02). */
    ComprobantePagoResponse pagarEnLinea(String numeroFactura, PagoEnLineaRequest solicitud);
}
