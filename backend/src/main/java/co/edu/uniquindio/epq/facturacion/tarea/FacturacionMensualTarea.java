package co.edu.uniquindio.epq.facturacion.tarea;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.comun.calendario.CalendarioLaboral;
import co.edu.uniquindio.epq.comun.tarea.TareaProgramadaTemplate;
import co.edu.uniquindio.epq.facturacion.FacturacionProperties;
import co.edu.uniquindio.epq.facturacion.repositorio.LoteFacturacionRepository;
import co.edu.uniquindio.epq.facturacion.servicio.GeneracionFacturacionService;
import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoGeneracionLoteResponse;

/**
 * Ejecuta automáticamente la facturación mensual (SWR-01 / RN-02).
 *
 * <p>Se evalúa diariamente; factura el mes de consumo anterior a partir del primer día hábil del mes
 * mientras su lote no esté completado. Así, si el servidor estuvo apagado ese día o el lote terminó
 * con errores, se recupera en la siguiente ejecución.</p>
 */
@Component
public class FacturacionMensualTarea extends TareaProgramadaTemplate {

    private final GeneracionFacturacionService generacionService;
    private final LoteFacturacionRepository loteRepository;
    private final CalendarioLaboral calendario;
    private final FacturacionProperties propiedades;
    private final Clock clock;

    public FacturacionMensualTarea(GeneracionFacturacionService generacionService,
                                   LoteFacturacionRepository loteRepository, CalendarioLaboral calendario,
                                   FacturacionProperties propiedades, Clock clock) {
        this.generacionService = generacionService;
        this.loteRepository = loteRepository;
        this.calendario = calendario;
        this.propiedades = propiedades;
        this.clock = clock;
    }

    @Scheduled(cron = "${app.facturacion.cron:0 0 1 * * *}", zone = "${app.zona-horaria:America/Bogota}")
    public void programar() {
        ejecutar();
    }

    @Override
    protected String nombre() {
        return "Facturación mensual";
    }

    @Override
    protected boolean debeEjecutarse() {
        if (!propiedades.ejecucionAutomatica()) {
            return false;
        }
        LocalDate hoy = LocalDate.now(clock);
        if (hoy.isBefore(calendario.primerDiaHabil(YearMonth.from(hoy)))) {
            return false;
        }
        return loteRepository.findByPeriodo(generacionService.periodoPorDefecto())
                .map(lote -> !lote.estaCompletado())
                .orElse(true);
    }

    @Override
    protected String ejecutarTarea() {
        ResultadoGeneracionLoteResponse resultado =
                generacionService.generarLote(null, GeneracionFacturacionService.ORIGEN_AUTOMATICO);
        return "Periodo %s: %d facturas nuevas, %d incidencias, %d sincronizadas con el ERP (%d ms)".formatted(
                resultado.periodo(), resultado.facturasGeneradas(), resultado.incidencias().size(),
                resultado.sincronizacionErp().exitosas(), resultado.duracionMs());
    }
}
