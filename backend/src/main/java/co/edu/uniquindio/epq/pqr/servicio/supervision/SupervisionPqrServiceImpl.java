package co.edu.uniquindio.epq.pqr.servicio.supervision;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comun.exportacion.ArchivoGenerado;
import co.edu.uniquindio.epq.comun.exportacion.ColumnaReporte;
import co.edu.uniquindio.epq.comun.exportacion.FabricaExportadores;
import co.edu.uniquindio.epq.comun.exportacion.FormatoExportacion;
import co.edu.uniquindio.epq.comun.exportacion.ReporteTabular;
import co.edu.uniquindio.epq.dashboard.indicadores.ConstructorIndicador;
import co.edu.uniquindio.epq.pqr.PqrProperties;
import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;
import co.edu.uniquindio.epq.pqr.web.dto.AlertaPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.CumplimientoPqrResponse;
import co.edu.uniquindio.epq.seguridad.Rol;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;
import co.edu.uniquindio.epq.seguridad.contexto.UsuarioAutenticado;

@Service
@Transactional(readOnly = true)
public class SupervisionPqrServiceImpl implements SupervisionPqrService {

    private static final long HORAS_EN_RIESGO = 5L * 24;

    private final PqrRepository pqrRepository;
    private final FabricaExportadores exportadores;
    private final PqrProperties propiedades;
    private final ContextoSeguridad contexto;
    private final Clock clock;

    public SupervisionPqrServiceImpl(PqrRepository pqrRepository, FabricaExportadores exportadores,
                                     PqrProperties propiedades, ContextoSeguridad contexto, Clock clock) {
        this.pqrRepository = pqrRepository;
        this.exportadores = exportadores;
        this.propiedades = propiedades;
        this.contexto = contexto;
        this.clock = clock;
    }

    @Override
    public List<AlertaPqrResponse> alertasDelEquipo(Integer jefeId) {
        // Un Jefe de PQR solo ve a su equipo; el Administrador puede elegir el jefe o ver toda la oficina
        Integer jefe = contexto.usuarioActual()
                .filter(u -> u.rol() == Rol.JEFE_PQR)
                .map(UsuarioAutenticado::gestorId)
                .orElse(jefeId);
        LocalDateTime ahora = LocalDateTime.now(clock);
        List<Pqr> pendientes = pqrRepository.buscarPendientesConGestor(EstadoPqrTipo.nombresPendientesDeRespuesta());
        return pendientes.stream()
                .filter(p -> jefe == null || (p.getGestor() != null && p.getGestor().getJefe() != null
                        && Objects.equals(p.getGestor().getJefe().getId(), jefe)))
                .map(p -> aAlerta(p, ahora))
                .sorted(Comparator.comparingLong(AlertaPqrResponse::horasRestantes))
                .toList();
    }

    @Override
    public CumplimientoPqrResponse cumplimiento(LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta == null ? LocalDate.now(clock) : hasta;
        LocalDate inicio = desde == null ? fin.withDayOfMonth(1).minusMonths(2) : desde;
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final");
        }
        List<Pqr> radicadas = pqrRepository.buscarRadicadasEntre(inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay());

        long respondidas = radicadas.stream().filter(p -> p.getFechaResolucion() != null).count();
        long enPlazo = radicadas.stream().filter(p -> Boolean.TRUE.equals(p.getRespondidaEnPlazo())).count();
        long fueraDePlazo = radicadas.stream()
                .filter(p -> p.getFechaResolucion() != null && Boolean.FALSE.equals(p.getRespondidaEnPlazo())).count();
        long vencidasSinRespuesta = radicadas.stream()
                .filter(p -> p.getFechaResolucion() == null && Boolean.FALSE.equals(p.getRespondidaEnPlazo())).count();
        long evaluadas = enPlazo + fueraDePlazo + vencidasSinRespuesta;

        return new CumplimientoPqrResponse(inicio, fin,
                new CumplimientoPqrResponse.Totales(radicadas.size(), respondidas, enPlazo, fueraDePlazo,
                        vencidasSinRespuesta, ConstructorIndicador.porcentaje(enPlazo, evaluadas)),
                agrupar(radicadas, p -> p.getTipoSolicitud().getNombre()),
                agrupar(radicadas, p -> p.getMunicipio() == null ? "Sin municipio" : p.getMunicipio().getNombre()),
                radicadas.stream().filter(p -> Boolean.FALSE.equals(p.getRespondidaEnPlazo()))
                        .map(SupervisionPqrServiceImpl::aIncumplimiento).toList());
    }

    @Override
    public ArchivoGenerado exportarIncumplimientos(LocalDate desde, LocalDate hasta, FormatoExportacion formato) {
        CumplimientoPqrResponse r = cumplimiento(desde, hasta);
        List<List<Object>> filas = r.incumplimientos().stream()
                .map(i -> List.<Object>of(i.radicado(), i.tipoSolicitud(), i.municipio() == null ? "" : i.municipio(),
                        i.gestor() == null ? "" : i.gestor(), i.fechaRecepcion(), i.fechaLimite(),
                        i.fechaRespuesta() == null ? "Sin respuesta" : i.fechaRespuesta(), i.estado()))
                .toList();
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("PQR radicadas", r.totales().radicadas());
        resumen.put("Respondidas dentro del plazo", r.totales().respondidasEnPlazo());
        resumen.put("Respondidas fuera del plazo", r.totales().respondidasFueraDePlazo());
        resumen.put("Vencidas sin respuesta", r.totales().vencidasSinRespuesta());
        resumen.put("Cumplimiento (%)", r.totales().porcentajeCumplimiento());
        ReporteTabular reporte = new ReporteTabular("cumplimiento-pqr-" + r.desde() + "-" + r.hasta(),
                "Cumplimiento del plazo normativo de PQR (reporte SSPD)",
                "PQR radicadas entre " + r.desde() + " y " + r.hasta() + " que incumplieron el plazo (RN-04)",
                LocalDateTime.now(clock), contexto.identificadorActual(),
                List.of(ColumnaReporte.texto("Radicado", 10), ColumnaReporte.texto("Tipo", 8),
                        ColumnaReporte.texto("Municipio", 9), ColumnaReporte.texto("Gestor", 14),
                        ColumnaReporte.fecha("Recepción", 10), ColumnaReporte.fecha("Fecha límite", 10),
                        ColumnaReporte.texto("Respuesta", 10), ColumnaReporte.texto("Estado", 8)),
                filas, resumen);
        return exportadores.exportar(reporte, formato);
    }

    private AlertaPqrResponse aAlerta(Pqr p, LocalDateTime ahora) {
        long horas = Duration.between(ahora, p.getFechaLimiteRespuesta()).toHours();
        String nivel = p.getEstadoTipo() == EstadoPqrTipo.VENCIDO || horas < 0 ? "VENCIDA"
                : horas <= propiedades.horasAlertaVencimiento() ? "CRITICA"
                : horas <= HORAS_EN_RIESGO ? "EN_RIESGO" : "EN_PLAZO";
        return new AlertaPqrResponse(p.getRadicado(), p.getTipoSolicitud().getNombre(), p.getEstado().getNombre(),
                p.getAsunto(), p.getGestor() == null ? null : p.getGestor().getNombre(),
                p.getGestor() == null ? null : p.getGestor().getId(),
                p.getMunicipio() == null ? null : p.getMunicipio().getNombre(), p.getFechaLimiteRespuesta(), horas,
                nivel, p.isNotificadoVencimiento());
    }

    private static List<CumplimientoPqrResponse.Agrupado> agrupar(List<Pqr> pqrs, Function<Pqr, String> clave) {
        Map<String, List<Pqr>> grupos = pqrs.stream()
                .collect(Collectors.groupingBy(clave, java.util.TreeMap::new, Collectors.toList()));
        return grupos.entrySet().stream().map(e -> {
            long enPlazo = e.getValue().stream().filter(p -> Boolean.TRUE.equals(p.getRespondidaEnPlazo())).count();
            long incumplidas = e.getValue().stream().filter(p -> Boolean.FALSE.equals(p.getRespondidaEnPlazo())).count();
            BigDecimal pct = ConstructorIndicador.porcentaje(enPlazo, enPlazo + incumplidas);
            return new CumplimientoPqrResponse.Agrupado(e.getKey(), e.getValue().size(), enPlazo, incumplidas, pct);
        }).toList();
    }

    private static CumplimientoPqrResponse.Incumplimiento aIncumplimiento(Pqr p) {
        return new CumplimientoPqrResponse.Incumplimiento(p.getRadicado(), p.getTipoSolicitud().getNombre(),
                p.getMunicipio() == null ? null : p.getMunicipio().getNombre(),
                p.getGestor() == null ? null : p.getGestor().getNombre(), p.getFechaRecepcion(),
                p.getFechaLimiteRespuesta(), p.getFechaResolucion(), p.getEstado().getNombre());
    }
}
