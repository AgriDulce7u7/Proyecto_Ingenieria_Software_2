package co.edu.uniquindio.epq.pqr.evento;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.pqr.dominio.HistorialPqr;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.repositorio.HistorialPqrRepository;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;

/**
 * Patrón <b>Observer</b>: registra en {@code historial_pqr} cada evento del ciclo de vida (SWR-09).
 *
 * <p>Se ejecuta de forma síncrona dentro de la misma transacción que originó el evento: si la
 * auditoría falla, la operación completa se revierte (no puede existir un cambio sin rastro).</p>
 */
@Component
public class AuditoriaPqrListener {

    private final PqrRepository pqrRepository;
    private final HistorialPqrRepository historialRepository;
    private final Clock clock;

    public AuditoriaPqrListener(PqrRepository pqrRepository, HistorialPqrRepository historialRepository,
                                Clock clock) {
        this.pqrRepository = pqrRepository;
        this.historialRepository = historialRepository;
        this.clock = clock;
    }

    @EventListener
    public void alRegistrar(PqrRegistradaEvento evento) {
        Pqr pqr = buscar(evento.pqrId());
        String canal = pqr.getCanal() != null ? pqr.getCanal().getNombre() : "sin canal";
        registrar(pqr, "PQR radicada con número %s por el canal %s".formatted(evento.radicado(), canal),
                evento.usuario());
    }

    @EventListener
    public void alAsignar(PqrAsignadaEvento evento) {
        Pqr pqr = buscar(evento.pqrId());
        String comentario = "Asignada al gestor %s".formatted(pqr.getGestor().getNombre());
        if (evento.motivo() != null && !evento.motivo().isBlank()) {
            comentario += ". Motivo: " + evento.motivo();
        }
        registrar(pqr, comentario, evento.usuario());
    }

    @EventListener
    public void alCambiarEstado(PqrEstadoCambiadoEvento evento) {
        Pqr pqr = buscar(evento.pqrId());
        String comentario = "%s → %s. %s".formatted(evento.estadoAnterior().getNombre(),
                evento.estadoNuevo().getNombre(), evento.comentario());
        registrar(pqr, comentario, evento.usuario());
    }

    private void registrar(Pqr pqr, String comentario, String usuario) {
        historialRepository.save(HistorialPqr.registrar(pqr, comentario, usuario, LocalDateTime.now(clock)));
    }

    private Pqr buscar(Long pqrId) {
        return pqrRepository.findById(pqrId).orElseThrow(() -> new RecursoNoEncontradoException("PQR", pqrId));
    }
}
