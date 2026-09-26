package co.edu.uniquindio.epq.facturacion.servicio.sincronizacion;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.facturacion.ErpProperties;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoSincronizacion;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.dominio.SincronizacionErp;
import co.edu.uniquindio.epq.facturacion.erp.ErpFinancieroGateway;
import co.edu.uniquindio.epq.facturacion.erp.FacturaErp;
import co.edu.uniquindio.epq.facturacion.erp.RespuestaErp;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.SincronizacionErpRepository;

/**
 * Envía una factura al ERP con reintentos, registra el intento en {@code sincronizacion_erp} y
 * actualiza el estado de la factura (SWR-04). Cada factura se confirma en su propia transacción.
 */
@Component
public class SincronizadorFacturaErp {

    private static final Logger log = LoggerFactory.getLogger(SincronizadorFacturaErp.class);

    private final FacturaRepository facturaRepository;
    private final SincronizacionErpRepository sincronizacionRepository;
    private final ErpFinancieroGateway erp;
    private final ErpProperties propiedades;
    private final Clock clock;

    public SincronizadorFacturaErp(FacturaRepository facturaRepository,
                                   SincronizacionErpRepository sincronizacionRepository,
                                   ErpFinancieroGateway erp, ErpProperties propiedades, Clock clock) {
        this.facturaRepository = facturaRepository;
        this.sincronizacionRepository = sincronizacionRepository;
        this.erp = erp;
        this.propiedades = propiedades;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultadoSincronizacionFactura sincronizar(Long facturaId) {
        Factura factura = facturaRepository.findWithDetalleById(facturaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura", facturaId));
        if (!factura.requiereSincronizacion()) {
            throw new ReglaNegocioException("La factura %s ya está en estado '%s'"
                    .formatted(factura.getNumeroFactura(), factura.getEstado().codigo()));
        }

        FacturaErp datos = aFormatoErp(factura);
        int maxIntentos = Math.max(1, propiedades.maxIntentos());
        int intento = 0;
        RespuestaErp respuesta = RespuestaErp.fallo("Sin respuesta del ERP");
        while (intento < maxIntentos) {
            intento++;
            respuesta = enviar(datos);
            if (respuesta.exitosa()) {
                break;
            }
            log.warn("Intento {}/{} fallido al sincronizar {}: {}", intento, maxIntentos,
                    factura.getNumeroFactura(), respuesta.mensaje());
            if (intento < maxIntentos) {
                esperar();
            }
        }

        if (respuesta.exitosa()) {
            factura.marcarSincronizada();
        } else {
            factura.marcarErrorSincronizacion();
        }
        String detalle = respuesta.exitosa()
                ? "Referencia ERP: %s. %s".formatted(respuesta.referencia(), respuesta.mensaje())
                : respuesta.mensaje();
        sincronizacionRepository.save(SincronizacionErp.registrar(factura,
                respuesta.exitosa() ? EstadoSincronizacion.EXITOSO : EstadoSincronizacion.FALLIDO,
                detalle, intento, LocalDateTime.now(clock)));

        return new ResultadoSincronizacionFactura(factura.getNumeroFactura(), respuesta.exitosa(), intento, detalle);
    }

    /** Las fallas técnicas (timeout, conexión rechazada) se tratan como un intento fallido. */
    private RespuestaErp enviar(FacturaErp datos) {
        try {
            return erp.enviarFactura(datos);
        } catch (RuntimeException ex) {
            return RespuestaErp.fallo("Error de comunicación con el ERP: " + ex.getMessage());
        }
    }

    private void esperar() {
        try {
            Thread.sleep(Math.max(0, propiedades.esperaEntreIntentosMs()));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static FacturaErp aFormatoErp(Factura factura) {
        return new FacturaErp(
                factura.getNumeroFactura(),
                factura.getPeriodo().toString(),
                factura.getContrato().getNumeroContrato(),
                factura.getContrato().getCliente().getNumeroDocumento(),
                factura.getContrato().getServicio().getNombre(),
                factura.getConsumo(),
                factura.getSubtotal(),
                factura.getTotal(),
                factura.getFechaVencimiento());
    }
}
