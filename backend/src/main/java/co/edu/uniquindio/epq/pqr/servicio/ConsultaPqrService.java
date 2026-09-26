package co.edu.uniquindio.epq.pqr.servicio;

import java.util.List;

import co.edu.uniquindio.epq.pqr.web.dto.ConsultaEstadoPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.HistorialPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.NotificacionResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrDetalleResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrResumenResponse;

/**
 * Consultas de solo lectura sobre PQR (RF-10, SWR-08).
 */
public interface ConsultaPqrService {

    ConsultaEstadoPqrResponse consultarEstadoPublico(String radicado);

    PqrDetalleResponse obtenerDetalle(String radicado);

    List<PqrResumenResponse> listar(FiltroPqr filtro);

    List<HistorialPqrResponse> obtenerHistorial(String radicado);

    List<NotificacionResponse> obtenerNotificaciones(String radicado);
}
