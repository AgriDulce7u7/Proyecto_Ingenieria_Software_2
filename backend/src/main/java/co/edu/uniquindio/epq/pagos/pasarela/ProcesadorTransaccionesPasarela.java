package co.edu.uniquindio.epq.pagos.pasarela;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.pagos.servicio.TimeoutPagoException;
import jakarta.annotation.PreDestroy;

/**
 * Aplica la política de CO-02 a cualquier pasarela: timeout de 30 segundos por intento y máximo
 * 2 intentos antes de reportar {@link TimeoutPagoException}. Los rechazos no se reintentan.
 * La misma referencia viaja en cada intento para que la pasarela no cobre dos veces.
 */
@Component
public class ProcesadorTransaccionesPasarela {

    private static final Logger log = LoggerFactory.getLogger(ProcesadorTransaccionesPasarela.class);

    private final PasarelaPagosGateway pasarela;
    private final PasarelaProperties propiedades;
    private final ExecutorService ejecutor = Executors.newVirtualThreadPerTaskExecutor();

    public ProcesadorTransaccionesPasarela(PasarelaPagosGateway pasarela, PasarelaProperties propiedades) {
        this.pasarela = pasarela;
        this.propiedades = propiedades;
    }

    public ResultadoCobro cobrar(SolicitudCobro solicitud) {
        int maximo = Math.max(1, propiedades.maxIntentos());
        for (int intento = 1; intento <= maximo; intento++) {
            CompletableFuture<RespuestaPasarela> llamada =
                    CompletableFuture.supplyAsync(() -> pasarela.cobrar(solicitud), ejecutor);
            try {
                return new ResultadoCobro(llamada.get(propiedades.timeoutSegundos(), TimeUnit.SECONDS), intento);
            } catch (TimeoutException ex) {
                llamada.cancel(true);
                log.warn("Intento {}/{}: la pasarela no respondió en {} s (referencia {})", intento, maximo,
                        propiedades.timeoutSegundos(), solicitud.referencia());
            } catch (ExecutionException ex) {
                if (!(ex.getCause() instanceof PasarelaNoDisponibleException)) {
                    throw new IllegalStateException("Error inesperado de la pasarela", ex.getCause());
                }
                log.warn("Intento {}/{}: {} (referencia {})", intento, maximo, ex.getCause().getMessage(),
                        solicitud.referencia());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new TimeoutPagoException(maximo);
    }

    @PreDestroy
    void cerrar() {
        ejecutor.shutdownNow();
    }

    /** Respuesta de la pasarela y número de intentos realizados. */
    public record ResultadoCobro(RespuestaPasarela respuesta, int intentos) {
    }
}
