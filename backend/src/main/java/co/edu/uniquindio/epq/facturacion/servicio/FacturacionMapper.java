package co.edu.uniquindio.epq.facturacion.servicio;

import co.edu.uniquindio.epq.facturacion.dominio.Contrato;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.dominio.SincronizacionErp;
import co.edu.uniquindio.epq.facturacion.web.dto.ContratoResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaDetalleResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaResumenResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.SincronizacionErpResponse;

/**
 * Conversión entidad → DTO (SRP: las entidades no conocen la capa web).
 */
public final class FacturacionMapper {

    private FacturacionMapper() {
    }

    public static ContratoResponse aContrato(Contrato contrato) {
        return new ContratoResponse(
                contrato.getId(),
                contrato.getNumeroContrato(),
                contrato.getServicio().getNombre(),
                contrato.getDireccionServicio(),
                contrato.getFechaInicio(),
                contrato.getEstado().name(),
                contrato.getCliente().getTipoDocumento(),
                contrato.getCliente().getNumeroDocumento(),
                contrato.getCliente().getNombreCompleto());
    }

    public static FacturaResumenResponse aResumen(Factura factura) {
        return new FacturaResumenResponse(
                factura.getNumeroFactura(),
                factura.getPeriodo().toString(),
                factura.getContrato().getNumeroContrato(),
                factura.getContrato().getCliente().getNombreCompleto(),
                factura.getContrato().getServicio().getNombre(),
                factura.getConsumo(),
                factura.getTotal(),
                factura.getFechaVencimiento(),
                factura.getEstado().name());
    }

    public static FacturaDetalleResponse aDetalle(Factura factura) {
        return new FacturaDetalleResponse(
                factura.getNumeroFactura(),
                factura.getPeriodo().toString(),
                factura.getEstado().name(),
                factura.getFechaGeneracion(),
                factura.getFechaVencimiento(),
                aContrato(factura.getContrato()),
                factura.getLectura().getMedidor().getNumeroSerie(),
                factura.getLectura().getLecturaAnterior(),
                factura.getLectura().getLecturaActual(),
                factura.getConsumo(),
                factura.getTarifa().getNombre(),
                factura.getTarifa().getValorPorUnidad(),
                factura.getValorConsumo(),
                factura.getCargoFijo(),
                factura.getSubtotal(),
                factura.getTotal());
    }

    public static SincronizacionErpResponse aSincronizacion(SincronizacionErp registro) {
        return new SincronizacionErpResponse(
                registro.getId(),
                registro.getFechaSincronizacion(),
                registro.getEstado().name(),
                registro.getIntentos(),
                registro.getRespuestaErp());
    }
}
