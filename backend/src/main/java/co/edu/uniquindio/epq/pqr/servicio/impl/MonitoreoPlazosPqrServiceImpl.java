package co.edu.uniquindio.epq.pqr.servicio.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.pqr.PqrProperties;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqr;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.evento.PqrEstadoCambiadoEvento;
import co.edu.uniquindio.epq.pqr.notificacion.ServicioNotificacionPqr;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;
import co.edu.uniquindio.epq.pqr.servicio.MonitoreoPlazosPqrService;
import co.edu.uniquindio.epq.pqr.servicio.ResolutorCatalogosPqr;
import co.edu.uniquindio.epq.pqr.web.dto.ResultadoMonitoreoResponse;

@Service
public class MonitoreoPlazosPqrServiceImpl implements MonitoreoPlazosPqrService {

    private final PqrRepository pqrRepository;
    private final ResolutorCatalogosPqr catalogos;
    private final ServicioNotificacionPqr notificaciones;
    private final ApplicationEventPublisher eventos;
    private final PqrProperties propiedades;
    private final Clock clock;

    public MonitoreoPlazosPqrServiceImpl(PqrRepository pqrRepository, ResolutorCatalogosPqr catalogos,
                                         ServicioNotificacionPqr notificaciones, ApplicationEventPublisher eventos,
                                         PqrProperties propiedades, Clock clock) {
        this.pqrRepository = pqrRepository;
        this.catalogos = catalogos;
        this.notificaciones = notificaciones;
        this.eventos = eventos;
        this.propiedades = propiedades;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ResultadoMonitoreoResponse ejecutarMonitoreo() {
        LocalDateTime ahora = LocalDateTime.now(clock);
        int vencidas = marcarVencidas(ahora);
        int alertas = enviarAlertas(ahora);
        return new ResultadoMonitoreoResponse(ahora, alertas, vencidas);
    }

    /** CU-06 (9a): el plazo se venció sin respuesta → estado Vencido, auditoría y aviso al gestor. */
    private int marcarVencidas(LocalDateTime ahora) {
        List<Pqr> vencidas = pqrRepository.buscarConPlazoVencido(EstadoPqrTipo.nombresConPlazoEnCurso(), ahora);
        if (vencidas.isEmpty()) {
            return 0;
        }
        EstadoPqr estadoVencido = catalogos.estado(EstadoPqrTipo.VENCIDO);
        for (Pqr pqr : vencidas) {
            EstadoPqrTipo anterior = pqr.getEstadoTipo();
            pqr.marcarVencida(estadoVencido);
            eventos.publishEvent(new PqrEstadoCambiadoEvento(pqr.getId(), anterior, EstadoPqrTipo.VENCIDO,
                    "Plazo normativo vencido sin respuesta (RN-04). Reportable a la SSPD.",
                    RegistroPqrServiceImpl.USUARIO_SISTEMA));
        }
        return vencidas.size();
    }

    /** SWR-07: alerta única al gestor cuando faltan N horas (48 por defecto) para el vencimiento. */
    private int enviarAlertas(LocalDateTime ahora) {
        LocalDateTime limite = ahora.plusHours(propiedades.horasAlertaVencimiento());
        List<Pqr> proximas = pqrRepository.buscarProximasAVencer(EstadoPqrTipo.nombresConPlazoEnCurso(), ahora, limite);
        for (Pqr pqr : proximas) {
            notificaciones.notificarAlertaVencimiento(pqr, ahora);
            pqr.marcarAlertaVencimientoEnviada();
        }
        return proximas.size();
    }
}
