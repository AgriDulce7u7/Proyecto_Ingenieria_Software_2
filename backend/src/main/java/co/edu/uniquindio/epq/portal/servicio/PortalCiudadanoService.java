package co.edu.uniquindio.epq.portal.servicio;

import java.util.List;

import co.edu.uniquindio.epq.comun.exportacion.ArchivoGenerado;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaDetalleResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaResumenResponse;
import co.edu.uniquindio.epq.pagos.web.dto.PagoResponse;
import co.edu.uniquindio.epq.portal.web.dto.ActualizarContactoRequest;
import co.edu.uniquindio.epq.portal.web.dto.ContratoPortalResponse;
import co.edu.uniquindio.epq.portal.web.dto.FacturaActualResponse;
import co.edu.uniquindio.epq.portal.web.dto.HistorialConsumosResponse;
import co.edu.uniquindio.epq.portal.web.dto.PerfilPortalResponse;

/**
 * F-06 / CU-05: consultas del ciudadano autenticado. Todas verifican que el recurso le pertenezca.
 */
public interface PortalCiudadanoService {

    PerfilPortalResponse perfil();

    PerfilPortalResponse actualizarContacto(ActualizarContactoRequest solicitud);

    List<ContratoPortalResponse> contratos();

    FacturaActualResponse facturaActual(String numeroContrato);

    HistorialConsumosResponse historialConsumos(String numeroContrato, int meses);

    List<FacturaResumenResponse> facturas(String numeroContrato);

    FacturaDetalleResponse factura(String numeroFactura);

    ArchivoGenerado facturaPdf(String numeroFactura);

    List<PagoResponse> pagos();
}
