package co.edu.uniquindio.epq.facturacion.servicio;

import java.util.List;

import co.edu.uniquindio.epq.facturacion.dominio.EstadoContrato;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoFactura;
import co.edu.uniquindio.epq.facturacion.web.dto.ContratoResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaDetalleResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaResumenResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.LoteFacturacionResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.ProgramacionFacturacionResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.SincronizacionErpResponse;

/**
 * Consultas de solo lectura del módulo de facturación (separadas de los comandos: ISP / CQS).
 */
public interface ConsultaFacturacionService {

    ProgramacionFacturacionResponse obtenerProgramacion();

    List<LoteFacturacionResponse> listarLotes();

    LoteFacturacionResponse obtenerLote(String periodo);

    List<FacturaResumenResponse> listarFacturasDelLote(String periodo);

    List<FacturaResumenResponse> buscarFacturas(String periodo, String numeroContrato, EstadoFactura estado,
                                                String documentoCliente);

    FacturaDetalleResponse obtenerFactura(String numeroFactura);

    List<SincronizacionErpResponse> obtenerSincronizaciones(String numeroFactura);

    List<ContratoResponse> listarContratos(EstadoContrato estado);
}
