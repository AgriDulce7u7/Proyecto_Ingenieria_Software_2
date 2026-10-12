package co.edu.uniquindio.epq.facturacion.documento;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.exportacion.ArchivoGenerado;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoCobroFactura;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;

/**
 * Genera el PDF de una factura (SWR-27). Lo usan el back-office y el portal ciudadano.
 */
@Component
public class GeneradorDocumentosFactura {

    private static final int MESES_HISTORIAL = 6;

    private final FacturaRepository facturaRepository;
    private final Clock clock;

    public GeneradorDocumentosFactura(FacturaRepository facturaRepository, Clock clock) {
        this.facturaRepository = facturaRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ArchivoGenerado facturaPdf(String numeroFactura) {
        Factura factura = facturaRepository.findByNumeroFactura(numeroFactura.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura", numeroFactura));
        return facturaPdf(factura);
    }

    @Transactional(readOnly = true)
    public ArchivoGenerado facturaPdf(Factura factura) {
        List<Factura> historial = facturaRepository.findByContratoIdOrderByPeriodoDesc(factura.getContrato().getId())
                .stream()
                .filter(f -> f.getEstadoCobro() != EstadoCobroFactura.ANULADA)
                .filter(f -> !f.getPeriodo().isAfter(factura.getPeriodo()))
                .limit(MESES_HISTORIAL)
                .toList();
        byte[] contenido = new FacturaPdf(factura, historial, LocalDateTime.now(clock)).generar();
        return new ArchivoGenerado("factura-" + factura.getNumeroFactura() + ".pdf", "application/pdf", contenido);
    }
}
