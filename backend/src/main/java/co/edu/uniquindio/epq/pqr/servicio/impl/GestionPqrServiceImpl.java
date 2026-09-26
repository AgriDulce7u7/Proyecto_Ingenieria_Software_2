package co.edu.uniquindio.epq.pqr.servicio.impl;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.evento.PqrAsignadaEvento;
import co.edu.uniquindio.epq.pqr.evento.PqrEstadoCambiadoEvento;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;
import co.edu.uniquindio.epq.pqr.servicio.GestionPqrService;
import co.edu.uniquindio.epq.pqr.servicio.PqrMapper;
import co.edu.uniquindio.epq.pqr.servicio.ResolutorCatalogosPqr;
import co.edu.uniquindio.epq.pqr.web.dto.CerrarPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.PqrDetalleResponse;
import co.edu.uniquindio.epq.pqr.web.dto.ReasignarPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.ResponderPqrRequest;
import co.edu.uniquindio.epq.pqr.web.dto.TomarPqrRequest;

/**
 * Operaciones del gestor sobre el ciclo de vida de la PQR. Las reglas de transición se delegan
 * al agregado {@link Pqr} (State); este servicio solo coordina y publica eventos.
 */
@Service
@Transactional
public class GestionPqrServiceImpl implements GestionPqrService {

    private final PqrRepository pqrRepository;
    private final ResolutorCatalogosPqr catalogos;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    public GestionPqrServiceImpl(PqrRepository pqrRepository, ResolutorCatalogosPqr catalogos,
                                 ApplicationEventPublisher eventos, Clock clock) {
        this.pqrRepository = pqrRepository;
        this.catalogos = catalogos;
        this.eventos = eventos;
        this.clock = clock;
    }

    @Override
    public PqrDetalleResponse tomar(String radicado, TomarPqrRequest solicitud) {
        Pqr pqr = buscar(radicado);
        Gestor gestor = catalogos.gestorActivo(solicitud.gestorId());
        EstadoPqrTipo anterior = pqr.getEstadoTipo();
        boolean cambiaGestor = !pqr.esGestionadaPor(gestor);

        pqr.iniciarTramite(catalogos.estado(EstadoPqrTipo.EN_TRAMITE), gestor);

        if (cambiaGestor) {
            eventos.publishEvent(new PqrAsignadaEvento(pqr.getId(), gestor.getId(),
                    "El gestor tomó la PQR", gestor.getCorreo()));
        }
        eventos.publishEvent(new PqrEstadoCambiadoEvento(pqr.getId(), anterior, EstadoPqrTipo.EN_TRAMITE,
                "Inicio de atención", gestor.getCorreo()));
        return PqrMapper.aDetalle(pqr);
    }

    @Override
    public PqrDetalleResponse reasignar(String radicado, ReasignarPqrRequest solicitud) {
        Pqr pqr = buscar(radicado);
        Gestor nuevoGestor = catalogos.gestorActivo(solicitud.gestorId());
        if (pqr.esGestionadaPor(nuevoGestor)) {
            throw new ReglaNegocioException("La PQR ya está asignada al gestor " + nuevoGestor.getNombre());
        }
        pqr.asignarA(nuevoGestor);
        eventos.publishEvent(new PqrAsignadaEvento(pqr.getId(), nuevoGestor.getId(), solicitud.motivo(),
                solicitud.usuario()));
        return PqrMapper.aDetalle(pqr);
    }

    @Override
    public PqrDetalleResponse responder(String radicado, ResponderPqrRequest solicitud) {
        Pqr pqr = buscar(radicado);
        Gestor gestor = catalogos.gestorActivo(solicitud.gestorId());
        if (!pqr.esGestionadaPor(gestor)) {
            throw new ReglaNegocioException("Solo el gestor asignado puede responder la PQR " + radicado);
        }
        EstadoPqrTipo anterior = pqr.getEstadoTipo();
        pqr.registrarRespuesta(solicitud.respuesta(), catalogos.estado(EstadoPqrTipo.RESUELTO),
                LocalDateTime.now(clock));

        String comentario = anterior == EstadoPqrTipo.VENCIDO
                ? "Respuesta extemporánea registrada"
                : "Respuesta formal registrada";
        eventos.publishEvent(new PqrEstadoCambiadoEvento(pqr.getId(), anterior, EstadoPqrTipo.RESUELTO,
                comentario, gestor.getCorreo()));
        return PqrMapper.aDetalle(pqr);
    }

    @Override
    public PqrDetalleResponse cerrar(String radicado, CerrarPqrRequest solicitud) {
        Pqr pqr = buscar(radicado);
        EstadoPqrTipo anterior = pqr.getEstadoTipo();
        pqr.cerrar(catalogos.estado(EstadoPqrTipo.CERRADO));

        String comentario = solicitud.observacion() == null || solicitud.observacion().isBlank()
                ? "Cierre del trámite"
                : "Cierre del trámite: " + solicitud.observacion().trim();
        eventos.publishEvent(new PqrEstadoCambiadoEvento(pqr.getId(), anterior, EstadoPqrTipo.CERRADO,
                comentario, solicitud.usuario()));
        return PqrMapper.aDetalle(pqr);
    }

    private Pqr buscar(String radicado) {
        return pqrRepository.findByRadicado(radicado.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("PQR con radicado", radicado));
    }
}
