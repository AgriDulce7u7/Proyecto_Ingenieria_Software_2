package co.edu.uniquindio.epq.pqr.servicio.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.pqr.dominio.CanalAtencion;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqr;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitud;
import co.edu.uniquindio.epq.pqr.repositorio.CanalAtencionRepository;
import co.edu.uniquindio.epq.pqr.repositorio.EstadoPqrRepository;
import co.edu.uniquindio.epq.pqr.repositorio.GestorRepository;
import co.edu.uniquindio.epq.pqr.repositorio.NotificacionRepository;
import co.edu.uniquindio.epq.pqr.repositorio.TipoSolicitudRepository;
import co.edu.uniquindio.epq.pqr.servicio.CatalogoPqrService;
import co.edu.uniquindio.epq.pqr.servicio.PqrMapper;
import co.edu.uniquindio.epq.pqr.web.dto.ElementoCatalogoResponse;
import co.edu.uniquindio.epq.pqr.web.dto.GestorResponse;
import co.edu.uniquindio.epq.pqr.web.dto.NotificacionResponse;

@Service
@Transactional(readOnly = true)
public class CatalogoPqrServiceImpl implements CatalogoPqrService {

    private final TipoSolicitudRepository tipoSolicitudRepository;
    private final CanalAtencionRepository canalRepository;
    private final EstadoPqrRepository estadoRepository;
    private final GestorRepository gestorRepository;
    private final NotificacionRepository notificacionRepository;

    public CatalogoPqrServiceImpl(TipoSolicitudRepository tipoSolicitudRepository,
                                  CanalAtencionRepository canalRepository, EstadoPqrRepository estadoRepository,
                                  GestorRepository gestorRepository, NotificacionRepository notificacionRepository) {
        this.tipoSolicitudRepository = tipoSolicitudRepository;
        this.canalRepository = canalRepository;
        this.estadoRepository = estadoRepository;
        this.gestorRepository = gestorRepository;
        this.notificacionRepository = notificacionRepository;
    }

    @Override
    public List<ElementoCatalogoResponse> tiposSolicitud() {
        return tipoSolicitudRepository.findAll().stream()
                .map((TipoSolicitud t) -> new ElementoCatalogoResponse(t.getId(), t.getTipo().name(), t.getNombre()))
                .toList();
    }

    @Override
    public List<ElementoCatalogoResponse> canales() {
        return canalRepository.findAll().stream()
                .map((CanalAtencion c) -> new ElementoCatalogoResponse(c.getId(), c.getTipo().name(), c.getNombre()))
                .toList();
    }

    @Override
    public List<ElementoCatalogoResponse> estados() {
        return estadoRepository.findAllByOrderByOrdenAsc().stream()
                .map((EstadoPqr e) -> new ElementoCatalogoResponse(e.getId(), e.getTipo().name(), e.getNombre()))
                .toList();
    }

    @Override
    public List<GestorResponse> gestoresActivos() {
        return gestorRepository.findByActivoTrueOrderByNombreAsc().stream().map(PqrMapper::aGestor).toList();
    }

    @Override
    public List<NotificacionResponse> notificacionesDeGestor(Integer gestorId) {
        if (!gestorRepository.existsById(gestorId)) {
            throw new RecursoNoEncontradoException("Gestor", gestorId);
        }
        return notificacionRepository.findByGestorIdOrderByFechaEnvioDescIdDesc(gestorId).stream()
                .map(PqrMapper::aNotificacion)
                .toList();
    }
}
