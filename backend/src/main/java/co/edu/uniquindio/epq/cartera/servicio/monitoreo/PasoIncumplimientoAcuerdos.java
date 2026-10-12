package co.edu.uniquindio.epq.cartera.servicio.monitoreo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.cartera.dominio.AcuerdoPago;
import co.edu.uniquindio.epq.cartera.dominio.EstadoAcuerdo;
import co.edu.uniquindio.epq.cartera.dominio.EstadoCuota;
import co.edu.uniquindio.epq.cartera.dominio.EstadoProcesoCobro;
import co.edu.uniquindio.epq.cartera.dominio.ProcesoCobro;
import co.edu.uniquindio.epq.cartera.repositorio.AcuerdoPagoRepository;
import co.edu.uniquindio.epq.cartera.repositorio.ProcesoCobroRepository;
import co.edu.uniquindio.epq.comun.notificacion.NotificacionDiferidaEvento;
import co.edu.uniquindio.epq.comun.notificacion.TipoNotificacionSistema;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.seguridad.Rol;

/**
 * SWR-31 / RN-07: una cuota no pagada en su fecha invalida el acuerdo, sus facturas vuelven a estar en
 * mora y se reactiva el cobro coercitivo. Se notifica al Gestor de Cartera y al usuario.
 */
@Component
public class PasoIncumplimientoAcuerdos implements PasoMonitoreoCartera {

    private final AcuerdoPagoRepository acuerdoRepository;
    private final ProcesoCobroRepository procesoRepository;
    private final FacturaRepository facturaRepository;
    private final ServicioAuditoria auditoria;
    private final ApplicationEventPublisher eventos;

    public PasoIncumplimientoAcuerdos(AcuerdoPagoRepository acuerdoRepository, ProcesoCobroRepository procesoRepository,
                                      FacturaRepository facturaRepository, ServicioAuditoria auditoria,
                                      ApplicationEventPublisher eventos) {
        this.acuerdoRepository = acuerdoRepository;
        this.procesoRepository = procesoRepository;
        this.facturaRepository = facturaRepository;
        this.auditoria = auditoria;
        this.eventos = eventos;
    }

    @Override
    public int orden() {
        return 20;
    }

    @Override
    public String nombre() {
        return "Incumplimiento de acuerdos de pago";
    }

    @Override
    @Transactional
    public ResultadoPaso ejecutar(LocalDate hoy, LocalDateTime ahora) {
        List<String> incumplidos = new ArrayList<>();
        for (AcuerdoPago acuerdo : acuerdoRepository.buscarConCuotaVencida(EstadoAcuerdo.VIGENTE, EstadoCuota.PENDIENTE, hoy)) {
            acuerdo.marcarIncumplido(hoy, ahora);
            List<Factura> facturas = facturaRepository.findByAcuerdoPagoIdOrderByPeriodoAsc(acuerdo.getId());
            facturas.forEach(Factura::liberarDeAcuerdo);

            String motivo = "Incumplimiento del acuerdo %s (RN-07)".formatted(acuerdo.getNumeroAcuerdo());
            procesoRepository.findByContratoId(acuerdo.getContrato().getId()).ifPresentOrElse(
                    p -> p.reactivar(motivo, ahora),
                    () -> procesoRepository.save(ProcesoCobro.iniciar(acuerdo.getContrato(), EstadoProcesoCobro.ACTIVO,
                            motivo, ahora)));

            String facturasTexto = String.join(", ", facturas.stream().map(Factura::getNumeroFactura).toList());
            auditoria.registrar(AccionAuditoria.INCUMPLIR_ACUERDO_PAGO, "acuerdo_pago", acuerdo.getNumeroAcuerdo(),
                    "Cuota vencida sin pago. Facturas que vuelven a mora: " + facturasTexto);
            auditoria.registrar(AccionAuditoria.REACTIVAR_COBRO_COERCITIVO, "contrato",
                    acuerdo.getContrato().getNumeroContrato(), motivo);
            eventos.publishEvent(NotificacionDiferidaEvento.aRol(Rol.GESTOR_CARTERA.name(),
                    TipoNotificacionSistema.ACUERDO_INCUMPLIDO, "SIGCA-EPQ - Acuerdo incumplido " + acuerdo.getNumeroAcuerdo(),
                    "El acuerdo %s del contrato %s fue incumplido. Se reactivó el cobro coercitivo (facturas: %s)."
                            .formatted(acuerdo.getNumeroAcuerdo(), acuerdo.getContrato().getNumeroContrato(), facturasTexto),
                    acuerdo.getNumeroAcuerdo()));
            eventos.publishEvent(NotificacionDiferidaEvento.aCorreo(acuerdo.getContrato().getCliente().getCorreo(),
                    TipoNotificacionSistema.ACUERDO_INCUMPLIDO, "EPQ - Su acuerdo de pago fue incumplido",
                    "Su acuerdo %s quedó sin efecto por una cuota vencida sin pago. Acérquese a EPQ para normalizar su deuda."
                            .formatted(acuerdo.getNumeroAcuerdo()), acuerdo.getNumeroAcuerdo()));
            incumplidos.add(acuerdo.getNumeroAcuerdo());
        }
        return new ResultadoPaso(incumplidos.size(),
                incumplidos.isEmpty() ? "Sin incumplimientos" : "Acuerdos incumplidos: " + String.join(", ", incumplidos));
    }
}
