package co.edu.uniquindio.epq.pagos.evento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.edu.uniquindio.epq.comun.exportacion.FormatosTexto;
import co.edu.uniquindio.epq.comun.notificacion.ServicioNotificacionSistema;
import co.edu.uniquindio.epq.comun.notificacion.TipoNotificacionSistema;
import co.edu.uniquindio.epq.pagos.dominio.Pago;
import co.edu.uniquindio.epq.pagos.repositorio.PagoRepository;

/**
 * Patrón <b>Observer</b>: envía el comprobante al correo del usuario cuando el pago ya quedó confirmado
 * (DS-03 paso 11). Un fallo del correo nunca revierte el pago.
 */
@Component
public class NotificacionPagoListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionPagoListener.class);

    private final PagoRepository pagoRepository;
    private final ServicioNotificacionSistema notificaciones;

    public NotificacionPagoListener(PagoRepository pagoRepository, ServicioNotificacionSistema notificaciones) {
        this.pagoRepository = pagoRepository;
        this.notificaciones = notificaciones;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void alRegistrarPago(PagoRegistradoEvento evento) {
        try {
            Pago pago = pagoRepository.findWithDetalleById(evento.pagoId()).orElseThrow();
            var cliente = pago.getFactura().getContrato().getCliente();
            notificaciones.notificarCorreo(cliente.getCorreo(), TipoNotificacionSistema.COMPROBANTE_PAGO,
                    "EPQ - Comprobante de pago " + pago.getNumeroComprobante(),
                    "Estimado(a) %s, confirmamos el pago de la factura %s por %s (canal %s) el %s. Comprobante: %s."
                            .formatted(cliente.getNombreCompleto(), pago.getFactura().getNumeroFactura(),
                                    FormatosTexto.moneda(pago.getValor()), pago.getCanal().name(),
                                    FormatosTexto.fechaHora(pago.getFechaPago()), pago.getNumeroComprobante()),
                    pago.getCodigoPago());
        } catch (RuntimeException ex) {
            log.error("No fue posible notificar el pago {}", evento.codigoPago(), ex);
        }
    }
}
