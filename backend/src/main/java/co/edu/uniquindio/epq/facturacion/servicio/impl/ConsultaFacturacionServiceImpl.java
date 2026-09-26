package co.edu.uniquindio.epq.facturacion.servicio.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comun.calendario.CalendarioLaboral;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.facturacion.FacturacionProperties;
import co.edu.uniquindio.epq.facturacion.dominio.Contrato;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoContrato;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoFactura;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.dominio.LoteFacturacion;
import co.edu.uniquindio.epq.facturacion.repositorio.ContratoRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaSpecifications;
import co.edu.uniquindio.epq.facturacion.repositorio.LoteFacturacionRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.SincronizacionErpRepository;
import co.edu.uniquindio.epq.facturacion.servicio.ConsultaFacturacionService;
import co.edu.uniquindio.epq.facturacion.servicio.FacturacionMapper;
import co.edu.uniquindio.epq.facturacion.servicio.PeriodoFacturacion;
import co.edu.uniquindio.epq.facturacion.web.dto.ContratoResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaDetalleResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaResumenResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.LoteFacturacionResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.ProgramacionFacturacionResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.SincronizacionErpResponse;

@Service
@Transactional(readOnly = true)
public class ConsultaFacturacionServiceImpl implements ConsultaFacturacionService {

    private static final Sort ORDEN_FACTURAS = Sort.by(Sort.Order.desc("periodo"), Sort.Order.asc("numeroFactura"));

    private final LoteFacturacionRepository loteRepository;
    private final FacturaRepository facturaRepository;
    private final SincronizacionErpRepository sincronizacionRepository;
    private final ContratoRepository contratoRepository;
    private final CalendarioLaboral calendario;
    private final FacturacionProperties propiedades;
    private final Clock clock;

    public ConsultaFacturacionServiceImpl(LoteFacturacionRepository loteRepository,
                                          FacturaRepository facturaRepository,
                                          SincronizacionErpRepository sincronizacionRepository,
                                          ContratoRepository contratoRepository, CalendarioLaboral calendario,
                                          FacturacionProperties propiedades, Clock clock) {
        this.loteRepository = loteRepository;
        this.facturaRepository = facturaRepository;
        this.sincronizacionRepository = sincronizacionRepository;
        this.contratoRepository = contratoRepository;
        this.calendario = calendario;
        this.propiedades = propiedades;
        this.clock = clock;
    }

    @Override
    public ProgramacionFacturacionResponse obtenerProgramacion() {
        LocalDate hoy = LocalDate.now(clock);
        YearMonth mesActual = YearMonth.from(hoy);
        LocalDate primerDiaHabil = calendario.primerDiaHabil(mesActual);
        YearMonth periodoPendiente = mesActual.minusMonths(1);
        Optional<LoteFacturacion> lotePendiente = loteRepository.findByPeriodo(periodoPendiente);
        boolean pendienteCompletado = lotePendiente.map(LoteFacturacion::estaCompletado).orElse(false);

        // Si el lote del mes anterior ya se completó, la próxima ejecución es el primer día hábil del mes siguiente.
        LocalDate proximaEjecucion;
        if (pendienteCompletado) {
            proximaEjecucion = calendario.primerDiaHabil(mesActual.plusMonths(1));
        } else {
            proximaEjecucion = hoy.isBefore(primerDiaHabil) ? primerDiaHabil : hoy;
        }

        return new ProgramacionFacturacionResponse(
                propiedades.ejecucionAutomatica(),
                hoy,
                hoy.equals(primerDiaHabil),
                primerDiaHabil,
                proximaEjecucion,
                periodoPendiente.toString(),
                lotePendiente.map(lote -> lote.getEstado().name()).orElse("SIN_EJECUTAR"),
                propiedades.tiempoMaximoLoteMinutos(),
                propiedades.diasParaVencimiento());
    }

    @Override
    public List<LoteFacturacionResponse> listarLotes() {
        return loteRepository.findAllByOrderByPeriodoDesc().stream().map(this::aLoteResponse).toList();
    }

    @Override
    public LoteFacturacionResponse obtenerLote(String periodo) {
        return aLoteResponse(buscarLote(periodo));
    }

    @Override
    public List<FacturaResumenResponse> listarFacturasDelLote(String periodo) {
        LoteFacturacion lote = buscarLote(periodo);
        return facturaRepository.findAll(FacturaSpecifications.delLote(lote.getId()), ORDEN_FACTURAS).stream()
                .map(FacturacionMapper::aResumen)
                .toList();
    }

    @Override
    public List<FacturaResumenResponse> buscarFacturas(String periodo, String numeroContrato, EstadoFactura estado,
                                                       String documentoCliente) {
        Specification<Factura> criterio = FacturaSpecifications.todas();
        if (periodo != null && !periodo.isBlank()) {
            criterio = criterio.and(FacturaSpecifications.delPeriodo(PeriodoFacturacion.parsear(periodo)));
        }
        if (numeroContrato != null && !numeroContrato.isBlank()) {
            criterio = criterio.and(FacturaSpecifications.delContrato(numeroContrato.trim()));
        }
        if (estado != null) {
            criterio = criterio.and(FacturaSpecifications.conEstado(estado));
        }
        if (documentoCliente != null && !documentoCliente.isBlank()) {
            criterio = criterio.and(FacturaSpecifications.delCliente(documentoCliente.trim()));
        }
        return facturaRepository.findAll(criterio, ORDEN_FACTURAS).stream()
                .map(FacturacionMapper::aResumen)
                .toList();
    }

    @Override
    public FacturaDetalleResponse obtenerFactura(String numeroFactura) {
        return FacturacionMapper.aDetalle(buscarFactura(numeroFactura));
    }

    @Override
    public List<SincronizacionErpResponse> obtenerSincronizaciones(String numeroFactura) {
        Factura factura = buscarFactura(numeroFactura);
        return sincronizacionRepository.findByFacturaIdOrderByFechaSincronizacionDesc(factura.getId()).stream()
                .map(FacturacionMapper::aSincronizacion)
                .toList();
    }

    @Override
    public List<ContratoResponse> listarContratos(EstadoContrato estado) {
        List<Contrato> contratos = estado == null
                ? contratoRepository.buscarTodosConDetalle()
                : contratoRepository.buscarPorEstadoConDetalle(estado);
        return contratos.stream().map(FacturacionMapper::aContrato).toList();
    }

    private LoteFacturacion buscarLote(String periodo) {
        YearMonth mes = PeriodoFacturacion.parsear(periodo);
        return loteRepository.findByPeriodo(mes)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lote de facturación", mes));
    }

    private Factura buscarFactura(String numeroFactura) {
        return facturaRepository.findByNumeroFactura(numeroFactura)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura", numeroFactura));
    }

    private LoteFacturacionResponse aLoteResponse(LoteFacturacion lote) {
        Duration duracion = lote.duracion();
        return new LoteFacturacionResponse(
                lote.getId(),
                lote.getPeriodo().toString(),
                lote.getEstado().name(),
                lote.getFechaInicioProceso(),
                lote.getFechaFinProceso(),
                duracion == null ? null : duracion.toSeconds(),
                lote.getTotalFacturasGeneradas(),
                facturaRepository.countByLoteIdAndEstado(lote.getId(), EstadoFactura.SINCRONIZADA),
                facturaRepository.countByLoteIdAndEstado(lote.getId(), EstadoFactura.ERROR_SINCRONIZACION),
                facturaRepository.countByLoteIdAndEstado(lote.getId(), EstadoFactura.GENERADA),
                facturaRepository.sumarTotalPorLote(lote.getId()));
    }
}
