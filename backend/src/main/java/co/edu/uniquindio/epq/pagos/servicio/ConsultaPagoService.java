package co.edu.uniquindio.epq.pagos.servicio;

import java.time.LocalDate;
import java.util.List;

import co.edu.uniquindio.epq.comun.exportacion.ArchivoGenerado;
import co.edu.uniquindio.epq.pagos.dominio.CanalPago;
import co.edu.uniquindio.epq.pagos.web.dto.ComprobantePagoResponse;
import co.edu.uniquindio.epq.pagos.web.dto.IntentoPagoResponse;
import co.edu.uniquindio.epq.pagos.web.dto.PagoResponse;

public interface ConsultaPagoService {

    List<PagoResponse> listar(String numeroFactura, String numeroContrato, CanalPago canal, LocalDate desde,
                              LocalDate hasta);

    ComprobantePagoResponse comprobante(String codigoPago);

    ArchivoGenerado comprobantePdf(String codigoPago);

    List<IntentoPagoResponse> intentosEnLinea(String numeroFactura);

    /** Comprobante de un pago del ciudadano autenticado (verifica que le pertenezca). */
    ComprobantePagoResponse comprobanteDelCiudadano(String codigoPago, Integer clienteId);

    ArchivoGenerado comprobantePdfDelCiudadano(String codigoPago, Integer clienteId);
}
