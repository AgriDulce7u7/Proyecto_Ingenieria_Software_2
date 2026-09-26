package co.edu.uniquindio.epq.pqr.servicio;

import java.util.List;

import co.edu.uniquindio.epq.pqr.web.dto.ElementoCatalogoResponse;
import co.edu.uniquindio.epq.pqr.web.dto.GestorResponse;
import co.edu.uniquindio.epq.pqr.web.dto.NotificacionResponse;

/**
 * Datos de apoyo para los formularios del frontend.
 */
public interface CatalogoPqrService {

    List<ElementoCatalogoResponse> tiposSolicitud();

    List<ElementoCatalogoResponse> canales();

    List<ElementoCatalogoResponse> estados();

    List<GestorResponse> gestoresActivos();

    List<NotificacionResponse> notificacionesDeGestor(Integer gestorId);
}
