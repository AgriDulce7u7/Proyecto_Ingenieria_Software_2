package co.edu.uniquindio.epq.facturacion.erp;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.facturacion.ErpProperties;

/**
 * Adaptador simulado del ERP para el prototipo académico. Acepta las facturas y devuelve una
 * referencia; con {@code app.erp.simulado.tasa-fallo} se pueden provocar fallos para demostrar
 * los reintentos y el estado {@code error_sincronizacion}.
 */
@Component
@ConditionalOnProperty(prefix = "app.erp", name = "modo", havingValue = "simulado", matchIfMissing = true)
public class ErpFinancieroSimuladoAdapter implements ErpFinancieroGateway {

    private static final Logger log = LoggerFactory.getLogger(ErpFinancieroSimuladoAdapter.class);

    private final double tasaFallo;

    public ErpFinancieroSimuladoAdapter(ErpProperties properties) {
        this.tasaFallo = Math.clamp(properties.simulado().tasaFallo(), 0.0, 1.0);
    }

    @Override
    public RespuestaErp enviarFactura(FacturaErp factura) {
        if (tasaFallo > 0 && ThreadLocalRandom.current().nextDouble() < tasaFallo) {
            log.debug("[ERP simulado] Rechazo simulado de la factura {}", factura.numeroFactura());
            return RespuestaErp.fallo("ERP simulado: servicio no disponible temporalmente");
        }
        String referencia = "ERP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.debug("[ERP simulado] Factura {} registrada con referencia {}", factura.numeroFactura(), referencia);
        return RespuestaErp.exito(referencia, "Factura registrada en el ERP simulado");
    }
}
