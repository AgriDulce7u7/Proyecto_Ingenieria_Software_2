package co.edu.uniquindio.epq.facturacion.servicio.generacion;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.facturacion.FacturacionProperties;
import co.edu.uniquindio.epq.facturacion.dominio.Contrato;
import co.edu.uniquindio.epq.facturacion.dominio.DetalleLiquidacion;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.dominio.LecturaMedidor;
import co.edu.uniquindio.epq.facturacion.dominio.LoteFacturacion;
import co.edu.uniquindio.epq.facturacion.dominio.Medidor;
import co.edu.uniquindio.epq.facturacion.dominio.Tarifa;
import co.edu.uniquindio.epq.facturacion.repositorio.ContratoRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.LecturaMedidorRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.LoteFacturacionRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.MedidorRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.TarifaRepository;
import co.edu.uniquindio.epq.facturacion.servicio.calculo.SelectorEstrategiaCalculo;
import co.edu.uniquindio.epq.facturacion.servicio.numeracion.GeneradorNumeroFactura;

/**
 * Factura un único contrato (SRP) en su propia transacción ({@code REQUIRES_NEW}): un error en un
 * contrato no revierte las facturas ya generadas del lote. Es idempotente: si el contrato ya tiene
 * factura para el periodo no se duplica.
 */
@Component
public class FacturadorContrato {

    private final ContratoRepository contratoRepository;
    private final MedidorRepository medidorRepository;
    private final LecturaMedidorRepository lecturaRepository;
    private final TarifaRepository tarifaRepository;
    private final FacturaRepository facturaRepository;
    private final LoteFacturacionRepository loteRepository;
    private final SelectorEstrategiaCalculo selectorEstrategia;
    private final GeneradorNumeroFactura generadorNumero;
    private final FacturacionProperties propiedades;
    private final Clock clock;

    public FacturadorContrato(ContratoRepository contratoRepository, MedidorRepository medidorRepository,
                              LecturaMedidorRepository lecturaRepository, TarifaRepository tarifaRepository,
                              FacturaRepository facturaRepository, LoteFacturacionRepository loteRepository,
                              SelectorEstrategiaCalculo selectorEstrategia, GeneradorNumeroFactura generadorNumero,
                              FacturacionProperties propiedades, Clock clock) {
        this.contratoRepository = contratoRepository;
        this.medidorRepository = medidorRepository;
        this.lecturaRepository = lecturaRepository;
        this.tarifaRepository = tarifaRepository;
        this.facturaRepository = facturaRepository;
        this.loteRepository = loteRepository;
        this.selectorEstrategia = selectorEstrategia;
        this.generadorNumero = generadorNumero;
        this.propiedades = propiedades;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultadoFacturacionContrato facturar(Integer contratoId, Integer loteId) {
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Contrato", contratoId));
        LoteFacturacion lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lote de facturación", loteId));
        YearMonth periodo = lote.getPeriodo();

        if (!contrato.esFacturable()) {
            return resultado(contrato, TipoResultadoFacturacion.NO_ACTIVO);
        }
        if (facturaRepository.existsByContratoIdAndPeriodo(contratoId, periodo)) {
            return resultado(contrato, TipoResultadoFacturacion.YA_FACTURADO);
        }

        Optional<Medidor> medidor = medidorRepository
                .findFirstByContratoIdAndEstadoOrderByFechaInstalacionDesc(contratoId, Medidor.ESTADO_ACTIVO);
        if (medidor.isEmpty()) {
            return resultado(contrato, TipoResultadoFacturacion.SIN_MEDIDOR);
        }

        Optional<LecturaMedidor> lectura = lecturaRepository.findByMedidorIdAndPeriodo(medidor.get().getId(), periodo);
        if (lectura.isEmpty()) {
            return new ResultadoFacturacionContrato(contratoId, contrato.getNumeroContrato(),
                    TipoResultadoFacturacion.SIN_LECTURA, null,
                    "No hay lectura del medidor %s para el periodo %s"
                            .formatted(medidor.get().getNumeroSerie(), periodo));
        }

        // La tarifa aplicable es la vigente al cierre del periodo de consumo (SWR-03).
        LocalDate fechaTarifa = periodo.atEndOfMonth();
        Optional<Tarifa> tarifa = tarifaRepository.buscarVigentes(contrato.getServicio().getId(), fechaTarifa)
                .stream().findFirst();
        if (tarifa.isEmpty()) {
            return new ResultadoFacturacionContrato(contratoId, contrato.getNumeroContrato(),
                    TipoResultadoFacturacion.SIN_TARIFA, null,
                    "No hay tarifa vigente de %s al %s".formatted(contrato.getServicio().getNombre(), fechaTarifa));
        }

        DetalleLiquidacion liquidacion = selectorEstrategia.seleccionar(contrato)
                .calcular(lectura.get(), tarifa.get());

        LocalDateTime ahora = LocalDateTime.now(clock);
        Factura factura = Factura.emitir(
                generadorNumero.generar(periodo, contrato), lote, contrato, lectura.get(), tarifa.get(),
                liquidacion, ahora, ahora.toLocalDate().plusDays(propiedades.diasParaVencimiento()));
        facturaRepository.save(factura);

        return new ResultadoFacturacionContrato(contratoId, contrato.getNumeroContrato(),
                TipoResultadoFacturacion.GENERADA, factura.getNumeroFactura(),
                "Total a pagar: " + factura.getTotal());
    }

    private static ResultadoFacturacionContrato resultado(Contrato contrato, TipoResultadoFacturacion tipo) {
        return ResultadoFacturacionContrato.de(contrato.getId(), contrato.getNumeroContrato(), tipo);
    }
}
