package co.edu.uniquindio.epq.pagos.servicio;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.secuencia.GeneradorConsecutivos;
import co.edu.uniquindio.epq.comun.secuencia.SecuenciaNegocio;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.pagos.dominio.CanalPago;
import co.edu.uniquindio.epq.pagos.dominio.Pago;
import co.edu.uniquindio.epq.pagos.evento.PagoRegistradoEvento;
import co.edu.uniquindio.epq.pagos.repositorio.PagoRepository;

/**
 * Aplica un pago confirmado a una factura (SRP). Lo usan el pago presencial y el pago en línea (DRY).
 *
 * <p>Bloquea la fila de la factura mientras aplica el pago: dos pagos simultáneos no pueden saldar la
 * misma factura (SWR-23). La restricción UNIQUE de {@code pago.factura_id} es la última garantía.</p>
 */
@Component
public class AplicadorPago {

    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;
    private final GeneradorConsecutivos consecutivos;
    private final ServicioAuditoria auditoria;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    public AplicadorPago(FacturaRepository facturaRepository, PagoRepository pagoRepository,
                         GeneradorConsecutivos consecutivos, ServicioAuditoria auditoria,
                         ApplicationEventPublisher eventos, Clock clock) {
        this.facturaRepository = facturaRepository;
        this.pagoRepository = pagoRepository;
        this.consecutivos = consecutivos;
        this.auditoria = auditoria;
        this.eventos = eventos;
        this.clock = clock;
    }

    @Transactional
    public Pago aplicar(String numeroFactura, BigDecimal valor, CanalPago canal, String codigoTransaccion,
                        String referencia, String usuario) {
        Factura factura = facturaRepository.buscarParaActualizar(numeroFactura.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura", numeroFactura));
        factura.validarPagable();   // SWR-23
        if (valor.compareTo(factura.getTotal()) != 0) {
            throw new ReglaNegocioException("El valor %s no coincide con el total de la factura %s (%s)"
                    .formatted(valor, factura.getNumeroFactura(), factura.getTotal()));
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        Pago pago = pagoRepository.save(Pago.registrar(
                "PAG-%09d".formatted(consecutivos.siguiente(SecuenciaNegocio.PAGO)), factura, valor, canal, ahora,
                codigoTransaccion, referencia,
                "CP-%09d".formatted(consecutivos.siguiente(SecuenciaNegocio.COMPROBANTE)), usuario));
        factura.registrarPago(ahora);   // RN-05: solo con confirmación

        auditoria.registrar(AccionAuditoria.REGISTRAR_PAGO, "factura", factura.getNumeroFactura(),
                "Pago %s por %s, canal %s%s. Comprobante %s".formatted(pago.getCodigoPago(), valor, canal,
                        codigoTransaccion == null ? "" : ", transacción " + codigoTransaccion,
                        pago.getNumeroComprobante()));
        eventos.publishEvent(new PagoRegistradoEvento(pago.getId(), pago.getCodigoPago()));
        return pago;
    }
}
