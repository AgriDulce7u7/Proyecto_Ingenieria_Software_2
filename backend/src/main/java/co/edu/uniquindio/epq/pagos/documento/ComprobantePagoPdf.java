package co.edu.uniquindio.epq.pagos.documento;

import java.time.LocalDateTime;

import co.edu.uniquindio.epq.comun.exportacion.DocumentoPdfEpq;
import co.edu.uniquindio.epq.comun.exportacion.FormatosTexto;
import co.edu.uniquindio.epq.pagos.web.dto.ComprobantePagoResponse;
import com.lowagie.text.Document;

/**
 * Comprobante electrónico de pago (SWR-22, RF-06).
 */
public class ComprobantePagoPdf extends DocumentoPdfEpq {

    private final ComprobantePagoResponse comprobante;

    public ComprobantePagoPdf(ComprobantePagoResponse comprobante, LocalDateTime generadoEn) {
        super(generadoEn);
        this.comprobante = comprobante;
    }

    @Override
    protected String titulo() {
        return "Comprobante de pago N.° " + comprobante.numeroComprobante();
    }

    @Override
    protected String subtitulo() {
        return "Pago " + comprobante.codigoPago() + " · " + FormatosTexto.fechaHora(comprobante.fechaPago());
    }

    @Override
    protected void agregarCuerpo(Document documento) throws Exception {
        documento.add(seccion("Datos del pago"));
        documento.add(tablaDatos(new String[][]{
                {"Valor pagado", FormatosTexto.moneda(comprobante.valor())},
                {"Canal", comprobante.canal()},
                {"Código de transacción", comprobante.codigoTransaccion() == null ? "-" : comprobante.codigoTransaccion()},
                {"Referencia", comprobante.referencia() == null ? "-" : comprobante.referencia()},
                {"Registrado por", comprobante.registradoPor()}
        }));
        documento.add(seccion("Factura pagada"));
        documento.add(tablaDatos(new String[][]{
                {"Factura", comprobante.numeroFactura()},
                {"Período", comprobante.periodo()},
                {"Contrato", comprobante.numeroContrato() + " (" + comprobante.servicio() + ")"},
                {"Usuario", comprobante.usuario() + " - " + comprobante.documentoUsuario()},
                {"Estado de la factura", comprobante.estadoFactura()}
        }));
    }
}
