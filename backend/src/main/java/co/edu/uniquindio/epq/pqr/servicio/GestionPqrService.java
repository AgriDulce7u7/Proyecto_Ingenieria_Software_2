package co.edu.uniquindio.epq.pqr.servicio;

import co.edu.uniquindio.epq.pqr.web.dto.CerrarPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.PqrDetalleResponse;
import co.edu.uniquindio.epq.pqr.web.dto.ReasignarPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.ResponderPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.TomarPqrRequest;

/**
 * Operaciones del Gestor de PQR sobre el ciclo de vida (RF-09, CU-06 pasos 6-10).
 */
public interface GestionPqrService {

    PqrDetalleResponse tomar(String radicado, TomarPqrRequest solicitud);

    PqrDetalleResponse reasignar(String radicado, ReasignarPqrRequest solicitud);

    PqrDetalleResponse responder(String radicado, ResponderPqrRequest solicitud);

    PqrDetalleResponse cerrar(String radicado, CerrarPqrRequest solicitud);
}
