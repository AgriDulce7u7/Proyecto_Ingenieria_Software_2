package co.edu.uniquindio.epq.pqr.servicio;

import java.util.List;

import co.edu.uniquindio.epq.pqr.dominio.Ciudadano;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.HistorialPqr;
import co.edu.uniquindio.epq.pqr.dominio.Notificacion;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.web.dto.CiudadanoResponse;
import co.edu.uniquindio.epq.pqr.web.dto.ConsultaEstadoPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.GestorResponse;
import co.edu.uniquindio.epq.pqr.web.dto.HistorialPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.NotificacionResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrDetalleResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrRegistradaResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrResumenResponse;

/**
 * Convierte entidades del dominio en DTOs de respuesta. Las entidades nunca se exponen en la API.
 */
public final class PqrMapper {

    private PqrMapper() {
    }

    public static PqrRegistradaResponse aRegistrada(Pqr pqr) {
        String gestor = pqr.getGestor() != null ? pqr.getGestor().getNombre() : null;
        return new PqrRegistradaResponse(
                pqr.getRadicado(),
                pqr.getTipoSolicitud().getNombre(),
                pqr.getEstado().getNombre(),
                pqr.getFechaRecepcion(),
                pqr.getFechaLimiteRespuesta(),
                pqr.getFechaEstimadaRespuesta(),
                gestor,
                "Su solicitud fue radicada con el número %s. Consérvelo para consultar el estado."
                        .formatted(pqr.getRadicado()));
    }

    public static ConsultaEstadoPqrResponse aConsultaPublica(Pqr pqr) {
        return new ConsultaEstadoPqrResponse(
                pqr.getRadicado(),
                pqr.getTipoSolicitud().getNombre(),
                pqr.getEstado().getNombre(),
                pqr.getFechaRecepcion(),
                pqr.getFechaEstimadaRespuesta(),
                pqr.getFechaResolucion());
    }

    public static PqrResumenResponse aResumen(Pqr pqr) {
        return new PqrResumenResponse(
                pqr.getRadicado(),
                pqr.getTipoSolicitud().getNombre(),
                pqr.getEstado().getNombre(),
                pqr.getAsunto(),
                pqr.getCiudadano().getNombreCompleto(),
                pqr.getGestor() != null ? pqr.getGestor().getNombre() : null,
                pqr.getFechaRecepcion(),
                pqr.getFechaLimiteRespuesta(),
                pqr.isNotificadoVencimiento());
    }

    public static PqrDetalleResponse aDetalle(Pqr pqr) {
        List<String> acciones = pqr.getEstadoTipo().transicionesPermitidas().stream()
                .filter(estado -> estado != EstadoPqrTipo.VENCIDO) // el vencimiento solo lo aplica el sistema
                .map(EstadoPqrTipo::name)
                .sorted()
                .toList();
        return new PqrDetalleResponse(
                pqr.getId(),
                pqr.getRadicado(),
                pqr.getTipoSolicitud().getNombre(),
                pqr.getCanal() != null ? pqr.getCanal().getNombre() : null,
                pqr.getEstado().getNombre(),
                pqr.getAsunto(),
                pqr.getDescripcion(),
                aCiudadano(pqr.getCiudadano()),
                pqr.getGestor() != null ? aGestor(pqr.getGestor()) : null,
                pqr.getFechaRecepcion(),
                pqr.getFechaLimiteRespuesta(),
                pqr.getFechaEstimadaRespuesta(),
                pqr.getFechaResolucion(),
                pqr.getRespuesta(),
                pqr.isNotificadoVencimiento(),
                acciones);
    }

    public static CiudadanoResponse aCiudadano(Ciudadano c) {
        return new CiudadanoResponse(c.getId(), c.getTipoDocumento(), c.getNumeroDocumento(), c.getNombreCompleto(),
                c.getCorreo(), c.getTelefono(), c.getDireccion());
    }

    public static GestorResponse aGestor(Gestor g) {
        return new GestorResponse(g.getId(), g.getNombre(), g.getCorreo(), g.getArea(), g.isActivo());
    }

    public static HistorialPqrResponse aHistorial(HistorialPqr h) {
        return new HistorialPqrResponse(h.getId(), h.getEstado().getNombre(), h.getFechaCambio(), h.getComentario(),
                h.getUsuarioCambio());
    }

    public static NotificacionResponse aNotificacion(Notificacion n) {
        String destinatario = n.getGestor() != null ? n.getGestor().getNombre() : "Ciudadano";
        return new NotificacionResponse(n.getId(), n.getPqr().getRadicado(), n.getTipo().codigo(), destinatario,
                n.getMensaje(), n.getFechaEnvio(), n.isEnviadoOk());
    }
}
