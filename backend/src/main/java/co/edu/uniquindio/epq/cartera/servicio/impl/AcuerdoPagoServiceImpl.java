package co.edu.uniquindio.epq.cartera.servicio.impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.cartera.dominio.AcuerdoPago;
import co.edu.uniquindio.epq.cartera.dominio.EstadoAcuerdo;
import co.edu.uniquindio.epq.cartera.dominio.EstadoProcesoCobro;
import co.edu.uniquindio.epq.cartera.dominio.PlanCuotas;
import co.edu.uniquindio.epq.cartera.dominio.ProcesoCobro;
import co.edu.uniquindio.epq.cartera.repositorio.AcuerdoPagoRepository;
import co.edu.uniquindio.epq.cartera.repositorio.ProcesoCobroRepository;
import co.edu.uniquindio.epq.cartera.servicio.AcuerdoPagoService;
import co.edu.uniquindio.epq.cartera.servicio.CarteraMapper;
import co.edu.uniquindio.epq.cartera.servicio.PoliticaCuotas;
import co.edu.uniquindio.epq.cartera.web.dto.AcuerdoPagoResponse;
import co.edu.uniquindio.epq.cartera.web.dto.PagarCuotaRequest;
import co.edu.uniquindio.epq.cartera.web.dto.PlanCuotasResponse;
import co.edu.uniquindio.epq.cartera.web.dto.ProcesoCobroResponse;
import co.edu.uniquindio.epq.cartera.web.dto.RegistrarAcuerdoRequest;
import co.edu.uniquindio.epq.comercial.dominio.Contrato;
import co.edu.uniquindio.epq.comercial.dominio.EstadoContrato;
import co.edu.uniquindio.epq.comercial.repositorio.ContratoRepository;
import co.edu.uniquindio.epq.comun.excepcion.RecursoDuplicadoException;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.exportacion.FormatosTexto;
import co.edu.uniquindio.epq.comun.notificacion.NotificacionDiferidaEvento;
import co.edu.uniquindio.epq.comun.notificacion.TipoNotificacionSistema;
import co.edu.uniquindio.epq.comun.secuencia.GeneradorConsecutivos;
import co.edu.uniquindio.epq.comun.secuencia.SecuenciaNegocio;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoCobroFactura;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;

/**
 * CU-08 Gestionar cobro persuasivo: registro de acuerdos (SWR-30) y pago de cuotas.
 * RN-07: un acuerdo vigente suspende el cobro coercitivo; al cumplirse, las facturas quedan pagadas.
 */
@Service
@Transactional
public class AcuerdoPagoServiceImpl implements AcuerdoPagoService {

    private final AcuerdoPagoRepository acuerdoRepository;
    private final ProcesoCobroRepository procesoRepository;
    private final ContratoRepository contratoRepository;
    private final FacturaRepository facturaRepository;
    private final PoliticaCuotas politicaCuotas;
    private final GeneradorConsecutivos consecutivos;
    private final ServicioAuditoria auditoria;
    private final ContextoSeguridad contexto;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    public AcuerdoPagoServiceImpl(AcuerdoPagoRepository acuerdoRepository, ProcesoCobroRepository procesoRepository,
                                  ContratoRepository contratoRepository, FacturaRepository facturaRepository,
                                  PoliticaCuotas politicaCuotas, GeneradorConsecutivos consecutivos,
                                  ServicioAuditoria auditoria, ContextoSeguridad contexto,
                                  ApplicationEventPublisher eventos, Clock clock) {
        this.acuerdoRepository = acuerdoRepository;
        this.procesoRepository = procesoRepository;
        this.contratoRepository = contratoRepository;
        this.facturaRepository = facturaRepository;
        this.politicaCuotas = politicaCuotas;
        this.consecutivos = consecutivos;
        this.auditoria = auditoria;
        this.contexto = contexto;
        this.eventos = eventos;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PlanCuotasResponse simular(String numeroContrato, Integer numeroCuotas, LocalDate fechaPrimeraCuota) {
        Contrato contrato = contrato(numeroContrato);
        List<Factura> vencidas = facturasEnMora(contrato);
        PlanCuotas plan = politicaCuotas.calcular(total(vencidas), numeroCuotas, fechaPrimeraCuota, LocalDate.now(clock));
        return new PlanCuotasResponse(contrato.getNumeroContrato(), plan.totalDeuda(),
                vencidas.stream().map(Factura::getNumeroFactura).toList(), plan.numeroCuotas(),
                plan.cuotas().stream().map(CarteraMapper::aCuota).toList());
    }

    @Override
    public AcuerdoPagoResponse registrar(RegistrarAcuerdoRequest s) {
        Contrato contrato = contrato(s.numeroContrato());
        if (contrato.getEstado() == EstadoContrato.CANCELADO) {
            throw new ReglaNegocioException("No se pueden registrar acuerdos sobre un contrato cancelado");
        }
        if (acuerdoRepository.existsByContratoIdAndEstado(contrato.getId(), EstadoAcuerdo.VIGENTE)) {
            throw new RecursoDuplicadoException("El contrato " + contrato.getNumeroContrato() + " ya tiene un acuerdo vigente");
        }
        List<Factura> vencidas = facturasEnMora(contrato);
        LocalDateTime ahora = LocalDateTime.now(clock);
        PlanCuotas plan = politicaCuotas.calcular(total(vencidas), s.numeroCuotas(), s.fechaPrimeraCuota(),
                ahora.toLocalDate());   // SWR-30: cuotas y fechas calculadas automáticamente

        String numero = "AP-%d-%04d".formatted(ahora.getYear(), consecutivos.siguiente(SecuenciaNegocio.ACUERDO_PAGO));
        AcuerdoPago acuerdo = acuerdoRepository.save(AcuerdoPago.registrar(numero, contrato, plan,
                s.observacion() == null ? null : s.observacion().trim(), contexto.identificadorActual(), ahora));
        vencidas.forEach(f -> f.incluirEnAcuerdo(acuerdo.getId()));   // DE-01: VENCIDA → PENDIENTE con acuerdo

        // RN-07: el acuerdo vigente suspende el cobro coercitivo
        String motivo = "Acuerdo de pago %s vigente (RN-07)".formatted(numero);
        procesoRepository.findByContratoId(contrato.getId()).ifPresentOrElse(
                p -> p.suspender(motivo, ahora),
                () -> procesoRepository.save(ProcesoCobro.iniciar(contrato, EstadoProcesoCobro.SUSPENDIDO, motivo, ahora)));
        contrato.actualizarCandidatoSuspension(false, ahora);

        auditoria.registrar(AccionAuditoria.REGISTRAR_ACUERDO_PAGO, "acuerdo_pago", numero,
                "Acuerdo para %s por %s en %d cuotas. Facturas: %s".formatted(contrato.getNumeroContrato(),
                        plan.totalDeuda(), plan.numeroCuotas(),
                        String.join(", ", vencidas.stream().map(Factura::getNumeroFactura).toList())));
        eventos.publishEvent(NotificacionDiferidaEvento.aCorreo(contrato.getCliente().getCorreo(),
                TipoNotificacionSistema.ACUERDO_REGISTRADO, "EPQ - Acuerdo de pago " + numero,
                "Se registró el acuerdo %s por %s en %d cuotas. Primera cuota: %s con vencimiento %s."
                        .formatted(numero, FormatosTexto.moneda(plan.totalDeuda()), plan.numeroCuotas(),
                                FormatosTexto.moneda(plan.cuotas().get(0).valor()),
                                FormatosTexto.fecha(plan.cuotas().get(0).fechaVencimiento())), numero));
        return CarteraMapper.aAcuerdo(acuerdo, vencidas.stream().map(Factura::getNumeroFactura).toList());
    }

    @Override
    public AcuerdoPagoResponse pagarCuota(String numeroAcuerdo, int numeroCuota, PagarCuotaRequest s) {
        AcuerdoPago acuerdo = acuerdo(numeroAcuerdo);
        LocalDateTime ahora = LocalDateTime.now(clock);
        BigDecimal valor = acuerdo.siguienteCuotaPendiente().getValor();
        boolean cumplido = acuerdo.pagarCuota(numeroCuota, s.valor(), s.canal(),
                s.referencia() == null || s.referencia().isBlank() ? null : s.referencia().trim(), ahora);
        auditoria.registrar(AccionAuditoria.PAGAR_CUOTA_ACUERDO, "acuerdo_pago", acuerdo.getNumeroAcuerdo(),
                "Cuota %d pagada por %s (canal %s)".formatted(numeroCuota, valor, s.canal()));

        List<Factura> facturas = facturaRepository.findByAcuerdoPagoIdOrderByPeriodoAsc(acuerdo.getId());
        if (cumplido) {
            facturas.forEach(f -> f.marcarPagadaPorAcuerdo(ahora));
            procesoRepository.findByContratoId(acuerdo.getContrato().getId())
                    .ifPresent(p -> p.cerrar("Acuerdo " + acuerdo.getNumeroAcuerdo() + " cumplido", ahora));
            auditoria.registrar(AccionAuditoria.CUMPLIR_ACUERDO_PAGO, "acuerdo_pago", acuerdo.getNumeroAcuerdo(),
                    "Acuerdo cumplido: facturas pagadas " + String.join(", ",
                            facturas.stream().map(Factura::getNumeroFactura).toList()));
            eventos.publishEvent(NotificacionDiferidaEvento.aCorreo(acuerdo.getContrato().getCliente().getCorreo(),
                    TipoNotificacionSistema.ACUERDO_CUMPLIDO, "EPQ - Acuerdo de pago cumplido",
                    "Su acuerdo %s quedó cumplido. Gracias por ponerse al día.".formatted(acuerdo.getNumeroAcuerdo()),
                    acuerdo.getNumeroAcuerdo()));
        }
        return CarteraMapper.aAcuerdo(acuerdo, facturas.stream().map(Factura::getNumeroFactura).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcuerdoPagoResponse> listar(EstadoAcuerdo estado, String numeroContrato) {
        return acuerdoRepository.findAllByOrderByFechaAcuerdoDesc().stream()
                .filter(a -> estado == null || a.getEstado() == estado)
                .filter(a -> numeroContrato == null || numeroContrato.isBlank()
                        || a.getContrato().getNumeroContrato().equals(numeroContrato.trim()))
                .map(a -> CarteraMapper.aAcuerdo(a, facturasDe(a)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AcuerdoPagoResponse obtener(String numeroAcuerdo) {
        AcuerdoPago acuerdo = acuerdo(numeroAcuerdo);
        return CarteraMapper.aAcuerdo(acuerdo, facturasDe(acuerdo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoCobroResponse> procesosCobro(EstadoProcesoCobro estado) {
        return (estado == null ? procesoRepository.findAllByOrderByFechaActualizacionDesc()
                : procesoRepository.findByEstadoOrderByFechaInicioAsc(estado)).stream()
                .map(CarteraMapper::aProceso)
                .toList();
    }

    // ------------------------------------------------------------------ apoyo

    private List<Factura> facturasEnMora(Contrato contrato) {
        List<Factura> vencidas = facturaRepository
                .findByContratoIdAndEstadoCobroOrderByPeriodoAsc(contrato.getId(), EstadoCobroFactura.VENCIDA).stream()
                .filter(Factura::estaEnMora)
                .toList();
        if (vencidas.isEmpty()) {
            throw new ReglaNegocioException("El contrato " + contrato.getNumeroContrato() + " no tiene facturas vencidas");
        }
        return vencidas;
    }

    private List<String> facturasDe(AcuerdoPago acuerdo) {
        return facturaRepository.findByAcuerdoPagoIdOrderByPeriodoAsc(acuerdo.getId()).stream()
                .map(Factura::getNumeroFactura).toList();
    }

    private static BigDecimal total(List<Factura> facturas) {
        return facturas.stream().map(Factura::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Contrato contrato(String numeroContrato) {
        return contratoRepository.findByNumeroContrato(numeroContrato.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Contrato", numeroContrato));
    }

    private AcuerdoPago acuerdo(String numeroAcuerdo) {
        return acuerdoRepository.findByNumeroAcuerdo(numeroAcuerdo.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Acuerdo de pago", numeroAcuerdo));
    }
}
