package co.edu.uniquindio.epq.pqr.servicio.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.repositorio.HistorialPqrRepository;
import co.edu.uniquindio.epq.pqr.repositorio.NotificacionRepository;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;
import co.edu.uniquindio.epq.pqr.repositorio.PqrSpecifications;
import co.edu.uniquindio.epq.pqr.servicio.ConsultaPqrService;
import co.edu.uniquindio.epq.pqr.servicio.FiltroPqr;
import co.edu.uniquindio.epq.pqr.servicio.PqrMapper;
import co.edu.uniquindio.epq.pqr.web.dto.ConsultaEstadoPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.HistorialPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.NotificacionResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrDetalleResponse;
import co.edu.uniquindio.epq.pqr.web.dto.PqrResumenResponse;

@Service
@Transactional(readOnly = true)
public class ConsultaPqrServiceImpl implements ConsultaPqrService {

    private final PqrRepository pqrRepository;
    private final HistorialPqrRepository historialRepository;
    private final NotificacionRepository notificacionRepository;

    public ConsultaPqrServiceImpl(PqrRepository pqrRepository, HistorialPqrRepository historialRepository,
                                  NotificacionRepository notificacionRepository) {
        this.pqrRepository = pqrRepository;
        this.historialRepository = historialRepository;
        this.notificacionRepository = notificacionRepository;
    }

    @Override
    public ConsultaEstadoPqrResponse consultarEstadoPublico(String radicado) {
        return PqrMapper.aConsultaPublica(buscar(radicado));
    }

    @Override
    public PqrDetalleResponse obtenerDetalle(String radicado) {
        return PqrMapper.aDetalle(buscar(radicado));
    }

    @Override
    public List<PqrResumenResponse> listar(FiltroPqr filtro) {
        Specification<Pqr> criterio = PqrSpecifications.todas();
        if (filtro.estado() != null) {
            criterio = criterio.and(PqrSpecifications.conEstado(filtro.estado()));
        }
        if (filtro.tipo() != null) {
            criterio = criterio.and(PqrSpecifications.conTipo(filtro.tipo()));
        }
        if (filtro.gestorId() != null) {
            criterio = criterio.and(PqrSpecifications.asignadaA(filtro.gestorId()));
        }
        if (filtro.documentoCiudadano() != null && !filtro.documentoCiudadano().isBlank()) {
            criterio = criterio.and(PqrSpecifications.delCiudadano(filtro.documentoCiudadano().trim()));
        }
        return pqrRepository.findAll(criterio, Sort.by(Sort.Direction.ASC, "fechaLimiteRespuesta"))
                .stream()
                .map(PqrMapper::aResumen)
                .toList();
    }

    @Override
    public List<HistorialPqrResponse> obtenerHistorial(String radicado) {
        Pqr pqr = buscar(radicado);
        return historialRepository.findByPqrIdOrderByFechaCambioAscIdAsc(pqr.getId()).stream()
                .map(PqrMapper::aHistorial)
                .toList();
    }

    @Override
    public List<NotificacionResponse> obtenerNotificaciones(String radicado) {
        Pqr pqr = buscar(radicado);
        return notificacionRepository.findByPqrIdOrderByFechaEnvioDescIdDesc(pqr.getId()).stream()
                .map(PqrMapper::aNotificacion)
                .toList();
    }

    private Pqr buscar(String radicado) {
        return pqrRepository.findByRadicado(radicado.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("PQR con radicado", radicado));
    }
}
