package co.edu.uniquindio.epq.pqr.tarea;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.comun.tarea.TareaProgramadaTemplate;
import co.edu.uniquindio.epq.pqr.servicio.MonitoreoPlazosPqrService;
import co.edu.uniquindio.epq.pqr.web.dto.ResultadoMonitoreoResponse;

/**
 * Revisa periódicamente los plazos de las PQR: envía la alerta de 48 horas (SWR-07) y marca como
 * vencidas las que superaron el plazo normativo (RN-04).
 */
@Component
public class MonitoreoPlazosPqrTarea extends TareaProgramadaTemplate {

    private final MonitoreoPlazosPqrService monitoreoService;

    public MonitoreoPlazosPqrTarea(MonitoreoPlazosPqrService monitoreoService) {
        this.monitoreoService = monitoreoService;
    }

    @Scheduled(cron = "${app.pqr.monitoreo-cron:0 */15 * * * *}", zone = "${app.zona-horaria:America/Bogota}")
    public void programar() {
        ejecutar();
    }

    @Override
    protected String nombre() {
        return "Monitoreo de plazos PQR";
    }

    @Override
    protected String ejecutarTarea() {
        ResultadoMonitoreoResponse resultado = monitoreoService.ejecutarMonitoreo();
        return "Alertas enviadas: %d. PQR vencidas: %d."
                .formatted(resultado.alertasEnviadas(), resultado.pqrMarcadasVencidas());
    }
}
