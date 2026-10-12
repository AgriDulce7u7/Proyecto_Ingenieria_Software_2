package co.edu.uniquindio.epq.pagos.servicio;

import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.pagos.dominio.IntentoPagoEnLinea;
import co.edu.uniquindio.epq.pagos.dominio.Pago;
import co.edu.uniquindio.epq.pagos.web.dto.ComprobantePagoResponse;
import co.edu.uniquindio.epq.pagos.web.dto.IntentoPagoResponse;
import co.edu.uniquindio.epq.pagos.web.dto.PagoResponse;

public final class PagosMapper {

    private PagosMapper() {
    }

    public static ComprobantePagoResponse aComprobante(Pago p) {
        Factura f = p.getFactura();
        return new ComprobantePagoResponse(p.getNumeroComprobante(), p.getCodigoPago(), p.getFechaPago(),
                f.getNumeroFactura(), f.getPeriodo().toString(), f.getContrato().getNumeroContrato(),
                f.getContrato().getServicio().getNombre(), f.getContrato().getCliente().getNombreCompleto(),
                f.getContrato().getCliente().getTipoDocumento() + " " + f.getContrato().getCliente().getNumeroDocumento(),
                p.getValor(), p.getCanal().name(), p.getCodigoTransaccion(), p.getReferencia(), p.getRegistradoPor(),
                f.getEstadoCobro().name());
    }

    public static PagoResponse aPago(Pago p) {
        Factura f = p.getFactura();
        return new PagoResponse(p.getCodigoPago(), p.getNumeroComprobante(), f.getNumeroFactura(),
                f.getContrato().getNumeroContrato(), f.getContrato().getCliente().getNombreCompleto(), p.getValor(),
                p.getCanal().name(), p.getFechaPago(), p.getCodigoTransaccion(), p.getRegistradoPor());
    }

    public static IntentoPagoResponse aIntento(IntentoPagoEnLinea i) {
        return new IntentoPagoResponse(i.getId(), i.getFecha(), i.getValor(), i.getResultado().name(),
                i.getCodigoTransaccion(), i.getCodigoRespuesta(), i.getMensaje(), i.getUltimosDigitos(), i.getIntentos(),
                i.getUsuario());
    }
}
