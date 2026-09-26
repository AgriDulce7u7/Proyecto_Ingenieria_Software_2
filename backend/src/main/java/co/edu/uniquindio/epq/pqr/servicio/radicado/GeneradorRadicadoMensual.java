package co.edu.uniquindio.epq.pqr.servicio.radicado;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;

/**
 * Genera radicados con formato {@code YYYYMM-NNNN} (CO-01), con consecutivo que reinicia cada mes.
 *
 * <p>La restricción UNIQUE de {@code pqr.radicado} garantiza la unicidad en base de datos. El
 * bloqueo {@code synchronized} reduce colisiones entre solicitudes simultáneas; si aun así dos
 * registros concurrentes obtienen el mismo consecutivo, el segundo falla con HTTP 409 y puede
 * reintentarse. En producción se recomienda una secuencia de PostgreSQL por mes.</p>
 */
@Component
public class GeneradorRadicadoMensual implements GeneradorRadicado {

    private static final DateTimeFormatter FORMATO_PREFIJO = DateTimeFormatter.ofPattern("yyyyMM");

    private final PqrRepository pqrRepository;

    public GeneradorRadicadoMensual(PqrRepository pqrRepository) {
        this.pqrRepository = pqrRepository;
    }

    @Override
    public synchronized String generar(LocalDateTime fechaRecepcion) {
        String prefijo = fechaRecepcion.format(FORMATO_PREFIJO) + "-";
        int siguiente = pqrRepository.findTopByRadicadoStartingWithOrderByRadicadoDesc(prefijo)
                .map(Pqr::getRadicado)
                .map(radicado -> Integer.parseInt(radicado.substring(prefijo.length())) + 1)
                .orElse(1);
        return prefijo + "%04d".formatted(siguiente);
    }
}
