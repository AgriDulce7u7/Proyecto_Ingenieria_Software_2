package co.edu.uniquindio.epq.facturacion.servicio.generacion;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.facturacion.dominio.LoteFacturacion;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.LoteFacturacionRepository;

/**
 * Administra el ciclo de vida del registro {@code lote_facturacion}. Cada operación se confirma de
 * inmediato ({@code REQUIRES_NEW}) para que el estado del lote sea visible mientras se procesa.
 */
@Component
public class GestorLoteFacturacion {

    private static final Logger log = LoggerFactory.getLogger(GestorLoteFacturacion.class);

    private final LoteFacturacionRepository loteRepository;
    private final FacturaRepository facturaRepository;
    private final Clock clock;

    public GestorLoteFacturacion(LoteFacturacionRepository loteRepository, FacturaRepository facturaRepository,
                                 Clock clock) {
        this.loteRepository = loteRepository;
        this.facturaRepository = facturaRepository;
        this.clock = clock;
    }

    /**
     * Abre el lote del periodo o reabre el existente (re-ejecución idempotente).
     *
     * <p>Quien invoca garantiza que no hay otra ejecución en curso; por eso un lote que aparezca
     * {@code EN_PROCESO} quedó huérfano (p. ej. el servidor se detuvo a mitad del proceso) y se
     * marca como error antes de reabrirlo.</p>
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LoteFacturacion abrir(YearMonth periodo) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        return loteRepository.findByPeriodo(periodo)
                .map(lote -> {
                    if (lote.estaEnProceso()) {
                        log.warn("El lote {} estaba EN_PROCESO de una ejecución interrumpida; se recupera", periodo);
                        lote.marcarError(ahora);
                    }
                    lote.reabrir(ahora);
                    return lote;
                })
                .orElseGet(() -> loteRepository.save(LoteFacturacion.abrir(periodo, ahora)));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LoteFacturacion finalizar(Integer loteId, boolean conErrores) {
        LoteFacturacion lote = buscar(loteId);
        int total = (int) facturaRepository.countByLoteId(loteId);
        lote.finalizar(total, conErrores, LocalDateTime.now(clock));
        return lote;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarError(Integer loteId) {
        buscar(loteId).marcarError(LocalDateTime.now(clock));
    }

    private LoteFacturacion buscar(Integer loteId) {
        return loteRepository.findById(loteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lote de facturación", loteId));
    }
}
