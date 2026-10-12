package co.edu.uniquindio.epq.cartera.servicio.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.edu.uniquindio.epq.cartera.servicio.MonitoreoCarteraService;
import co.edu.uniquindio.epq.cartera.servicio.monitoreo.PasoMonitoreoCartera;
import co.edu.uniquindio.epq.cartera.web.dto.ResultadoMonitoreoCarteraResponse;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;

/**
 * Ejecuta los pasos del monitoreo en orden. No es transaccional: cada paso confirma su propia
 * transacción para que el siguiente vea sus cambios. Un paso que falla no impide los demás.
 */
@Service
public class MonitoreoCarteraServiceImpl implements MonitoreoCarteraService {

    private static final Logger log = LoggerFactory.getLogger(MonitoreoCarteraServiceImpl.class);

    private final List<PasoMonitoreoCartera> pasos;
    private final ReentrantLock candado = new ReentrantLock();
    private final Clock clock;

    public MonitoreoCarteraServiceImpl(List<PasoMonitoreoCartera> pasos, Clock clock) {
        this.pasos = pasos.stream().sorted(Comparator.comparingInt(PasoMonitoreoCartera::orden)).toList();
        this.clock = clock;
    }

    @Override
    public ResultadoMonitoreoCarteraResponse ejecutar() {
        if (!candado.tryLock()) {
            throw new ReglaNegocioException("El monitoreo de cartera ya se está ejecutando");
        }
        try {
            LocalDateTime ahora = LocalDateTime.now(clock);
            List<ResultadoMonitoreoCarteraResponse.Paso> resultados = new ArrayList<>();
            for (PasoMonitoreoCartera paso : pasos) {
                try {
                    PasoMonitoreoCartera.ResultadoPaso r = paso.ejecutar(ahora.toLocalDate(), ahora);
                    resultados.add(new ResultadoMonitoreoCarteraResponse.Paso(paso.nombre(), r.registrosAfectados(), r.detalle()));
                } catch (RuntimeException ex) {
                    log.error("Falló el paso '{}' del monitoreo de cartera", paso.nombre(), ex);
                    resultados.add(new ResultadoMonitoreoCarteraResponse.Paso(paso.nombre(), 0, "ERROR: " + ex.getMessage()));
                }
            }
            return new ResultadoMonitoreoCarteraResponse(ahora, resultados);
        } finally {
            candado.unlock();
        }
    }
}
