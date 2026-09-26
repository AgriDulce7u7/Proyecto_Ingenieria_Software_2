package co.edu.uniquindio.epq.pqr.servicio.impl;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.pqr.dominio.CanalAtencionTipo;
import co.edu.uniquindio.epq.pqr.dominio.Ciudadano;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.PlazoRespuesta;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.evento.PqrAsignadaEvento;
import co.edu.uniquindio.epq.pqr.evento.PqrRegistradaEvento;
import co.edu.uniquindio.epq.pqr.repositorio.CiudadanoRepository;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;
import co.edu.uniquindio.epq.pqr.servicio.PqrMapper;
import co.edu.uniquindio.epq.pqr.servicio.RegistroPqrService;
import co.edu.uniquindio.epq.pqr.servicio.ResolutorCatalogosPqr;
import co.edu.uniquindio.epq.pqr.servicio.asignacion.EstrategiaAsignacionGestor;
import co.edu.uniquindio.epq.pqr.servicio.plazo.CalculadoraPlazoRespuesta;
import co.edu.uniquindio.epq.pqr.servicio.radicado.GeneradorRadicado;
import co.edu.uniquindio.epq.pqr.web.dto.PqrRegistradaResponse;
import co.edu.uniquindio.epq.pqr.web.dto.RegistrarPqrRequest;

/**
 * Registra una PQR siguiendo DS-02: valida, genera radicado, calcula plazo, asigna gestor y
 * publica eventos. La auditoría y las notificaciones las realizan los observadores de eventos,
 * por lo que este servicio no depende de ellos (SRP, bajo acoplamiento).
 */
@Service
public class RegistroPqrServiceImpl implements RegistroPqrService {

    static final String USUARIO_SISTEMA = "SISTEMA";

    private final PqrRepository pqrRepository;
    private final CiudadanoRepository ciudadanoRepository;
    private final ResolutorCatalogosPqr catalogos;
    private final CalculadoraPlazoRespuesta calculadoraPlazo;
    private final GeneradorRadicado generadorRadicado;
    private final EstrategiaAsignacionGestor estrategiaAsignacion;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    public RegistroPqrServiceImpl(PqrRepository pqrRepository, CiudadanoRepository ciudadanoRepository,
                                  ResolutorCatalogosPqr catalogos, CalculadoraPlazoRespuesta calculadoraPlazo,
                                  GeneradorRadicado generadorRadicado,
                                  EstrategiaAsignacionGestor estrategiaAsignacion,
                                  ApplicationEventPublisher eventos, Clock clock) {
        this.pqrRepository = pqrRepository;
        this.ciudadanoRepository = ciudadanoRepository;
        this.catalogos = catalogos;
        this.calculadoraPlazo = calculadoraPlazo;
        this.generadorRadicado = generadorRadicado;
        this.estrategiaAsignacion = estrategiaAsignacion;
        this.eventos = eventos;
        this.clock = clock;
    }

    @Override
    @Transactional
    public PqrRegistradaResponse registrar(RegistrarPqrRequest solicitud) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        Ciudadano ciudadano = obtenerOCrearCiudadano(solicitud, ahora);
        CanalAtencionTipo canal = solicitud.canal() != null ? solicitud.canal() : CanalAtencionTipo.WEB;
        PlazoRespuesta plazo = calculadoraPlazo.calcular(solicitud.tipoSolicitud(), ahora);

        Pqr pqr = Pqr.radicar(
                generadorRadicado.generar(ahora),
                ciudadano,
                catalogos.tipoSolicitud(solicitud.tipoSolicitud()),
                catalogos.canal(canal),
                catalogos.estado(EstadoPqrTipo.RADICADO),
                solicitud.asunto().trim(),
                solicitud.descripcion().trim(),
                ahora,
                plazo);
        pqrRepository.save(pqr);
        eventos.publishEvent(new PqrRegistradaEvento(pqr.getId(), pqr.getRadicado(), usuarioCiudadano(ciudadano)));

        estrategiaAsignacion.seleccionarPara(pqr).ifPresent(gestor -> {
            pqr.asignarA(gestor);
            eventos.publishEvent(new PqrAsignadaEvento(pqr.getId(), gestor.getId(),
                    "Asignación automática por menor carga", USUARIO_SISTEMA));
        });
        return PqrMapper.aRegistrada(pqr);
    }

    private Ciudadano obtenerOCrearCiudadano(RegistrarPqrRequest s, LocalDateTime ahora) {
        return ciudadanoRepository
                .findByTipoDocumentoAndNumeroDocumento(s.tipoDocumento(), s.numeroDocumento().trim())
                .map(existente -> {
                    existente.actualizarContacto(s.nombreCompleto(), s.correo(), s.telefono(), s.direccion());
                    return existente;
                })
                .orElseGet(() -> ciudadanoRepository.save(Ciudadano.registrar(
                        s.tipoDocumento(), s.numeroDocumento().trim(), s.nombreCompleto().trim(),
                        s.correo().trim(), limpiar(s.telefono()), limpiar(s.direccion()), ahora)));
    }

    private static String usuarioCiudadano(Ciudadano ciudadano) {
        return "ciudadano:%s-%s".formatted(ciudadano.getTipoDocumento(), ciudadano.getNumeroDocumento());
    }

    private static String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
