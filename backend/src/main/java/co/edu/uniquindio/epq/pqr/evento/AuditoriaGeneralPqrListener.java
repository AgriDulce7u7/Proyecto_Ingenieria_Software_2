package co.edu.uniquindio.epq.pqr.evento;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;

/**
 * Patrón <b>Observer</b>: lleva los eventos de la PQR al log de auditoría general (RF-15: registro y
 * respuesta de PQR). Complementa al historial propio de la PQR ({@link AuditoriaPqrListener}) sin modificar
 * los servicios que publican los eventos (OCP).
 */
@Component
public class AuditoriaGeneralPqrListener {

    private static final String ENTIDAD = "pqr";

    private final ServicioAuditoria auditoria;
    private final PqrRepository pqrRepository;
    private final ContextoSeguridad contexto;

    public AuditoriaGeneralPqrListener(ServicioAuditoria auditoria, PqrRepository pqrRepository,
                                       ContextoSeguridad contexto) {
        this.auditoria = auditoria;
        this.pqrRepository = pqrRepository;
        this.contexto = contexto;
    }

    @EventListener
    public void alRegistrar(PqrRegistradaEvento evento) {
        registrar(evento.usuario(), AccionAuditoria.REGISTRAR_PQR, evento.radicado(), "PQR radicada");
    }

    @EventListener
    public void alAsignar(PqrAsignadaEvento evento) {
        registrar(evento.usuario(), AccionAuditoria.ASIGNAR_PQR, radicado(evento.pqrId()),
                "Asignada al gestor " + evento.gestorId() + (evento.motivo() == null ? "" : ". " + evento.motivo()));
    }

    @EventListener
    public void alCambiarEstado(PqrEstadoCambiadoEvento evento) {
        AccionAuditoria accion = switch (evento.estadoNuevo()) {
            case RESUELTO -> AccionAuditoria.RESPONDER_PQR;
            case VENCIDO -> AccionAuditoria.VENCER_PQR;
            default -> AccionAuditoria.CAMBIAR_ESTADO_PQR;
        };
        registrar(evento.usuario(), accion, radicado(evento.pqrId()),
                "%s → %s. %s".formatted(evento.estadoAnterior().getNombre(), evento.estadoNuevo().getNombre(),
                        evento.comentario()));
    }

    private void registrar(String usuarioEvento, AccionAuditoria accion, String radicado, String detalle) {
        String usuario = contexto.usuarioActual().isPresent() ? contexto.identificadorActual() : usuarioEvento;
        String rol = contexto.usuarioActual().map(u -> u.rol().name()).orElse(null);
        auditoria.registrarComo(usuario, rol, accion, ENTIDAD, radicado, detalle, true);
    }

    private String radicado(Long pqrId) {
        return pqrRepository.findById(pqrId).map(p -> p.getRadicado()).orElse(String.valueOf(pqrId));
    }
}
