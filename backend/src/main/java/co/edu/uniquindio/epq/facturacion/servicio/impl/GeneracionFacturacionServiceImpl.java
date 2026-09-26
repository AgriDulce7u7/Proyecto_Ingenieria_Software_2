package co.edu.uniquindio.epq.facturacion.servicio.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.facturacion.FacturacionProperties;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoContrato;
import co.edu.uniquindio.epq.facturacion.dominio.LoteFacturacion;
import co.edu.uniquindio.epq.facturacion.repositorio.ContratoRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.facturacion.servicio.GeneracionFacturacionService;
import co.edu.uniquindio.epq.facturacion.servicio.SincronizacionErpService;
import co.edu.uniquindio.epq.facturacion.servicio.generacion.FacturadorContrato;
import co.edu.uniquindio.epq.facturacion.servicio.generacion.GestorLoteFacturacion;
import co.edu.uniquindio.epq.facturacion.servicio.generacion.ResultadoFacturacionContrato;
import co.edu.uniquindio.epq.facturacion.servicio.generacion.TipoResultadoFacturacion;
import co.edu.uniquindio.epq.facturacion.web.dto.IncidenciaFacturacionResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoGeneracionLoteResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoSincronizacionResponse;

/**
 * Patrón <b>Facade</b>: un único punto de entrada coordina el lote, la liquidación de cada contrato
 * y la sincronización con el ERP. Los detalles están delegados en componentes con una sola
 * responsabilidad ({@link GestorLoteFacturacion}, {@link FacturadorContrato}, {@link SincronizacionErpService}).
 */
@Service
public class GeneracionFacturacionServiceImpl implements GeneracionFacturacionService {

    private static final Logger log = LoggerFactory.getLogger(GeneracionFacturacionServiceImpl.class);

    /** Evita dos ejecuciones simultáneas del lote dentro de la misma instancia. */
    private final ReentrantLock candado = new ReentrantLock();

    private final ContratoRepository contratoRepository;
    private final FacturaRepository facturaRepository;
    private final GestorLoteFacturacion gestorLote;
    private final FacturadorContrato facturador;
    private final SincronizacionErpService sincronizacion;
    private final FacturacionProperties propiedades;
    private final Clock clock;

    public GeneracionFacturacionServiceImpl(ContratoRepository contratoRepository, FacturaRepository facturaRepository,
                                            GestorLoteFacturacion gestorLote, FacturadorContrato facturador,
                                            SincronizacionErpService sincronizacion,
                                            FacturacionProperties propiedades, Clock clock) {
        this.contratoRepository = contratoRepository;
        this.facturaRepository = facturaRepository;
        this.gestorLote = gestorLote;
        this.facturador = facturador;
        this.sincronizacion = sincronizacion;
        this.propiedades = propiedades;
        this.clock = clock;
    }

    @Override
    public YearMonth periodoPorDefecto() {
        return YearMonth.now(clock).minusMonths(1);
    }

    @Override
    public ResultadoGeneracionLoteResponse generarLote(YearMonth periodo, String origen) {
        YearMonth periodoAFacturar = periodo == null ? periodoPorDefecto() : periodo;
        if (periodoAFacturar.isAfter(YearMonth.now(clock))) {
            throw new ReglaNegocioException("No se puede facturar un periodo futuro: " + periodoAFacturar);
        }
        if (!candado.tryLock()) {
            throw new ReglaNegocioException("Ya hay una generación de facturas en curso. Intente más tarde.");
        }
        try {
            return procesar(periodoAFacturar, origen);
        } finally {
            candado.unlock();
        }
    }

    private ResultadoGeneracionLoteResponse procesar(YearMonth periodo, String origen) {
        LocalDateTime inicio = LocalDateTime.now(clock);
        LoteFacturacion lote = gestorLote.abrir(periodo);
        log.info("Lote {} ({}) iniciado. Origen: {}", periodo, lote.getId(), origen);

        try {
            List<Integer> contratosActivos = contratoRepository.buscarIdsPorEstado(EstadoContrato.ACTIVO);
            List<ResultadoFacturacionContrato> resultados = new ArrayList<>(contratosActivos.size());
            for (Integer contratoId : contratosActivos) {
                resultados.add(facturarAislado(contratoId, lote.getId()));
            }
            boolean huboErrores = resultados.stream().anyMatch(r -> r.tipo() == TipoResultadoFacturacion.ERROR);
            LoteFacturacion finalizado = gestorLote.finalizar(lote.getId(), huboErrores);

            // CU-03 <<include>> Integración ERP (SWR-04)
            ResultadoSincronizacionResponse sincronizacionErp = sincronizacion.sincronizarLote(periodo.toString());

            LocalDateTime fin = LocalDateTime.now(clock);
            Duration duracion = Duration.between(inicio, fin);
            boolean dentroDelTiempo = duracion.toMinutes() < propiedades.tiempoMaximoLoteMinutos();
            if (!dentroDelTiempo) {
                log.warn("SWR-02: el lote {} tardó {} min (máximo {} min)", periodo, duracion.toMinutes(),
                        propiedades.tiempoMaximoLoteMinutos());
            }

            return new ResultadoGeneracionLoteResponse(
                    periodo.toString(),
                    origen,
                    finalizado.getEstado().name(),
                    inicio,
                    fin,
                    duracion.toMillis(),
                    dentroDelTiempo,
                    contratosActivos.size(),
                    contratoRepository.count() - contratosActivos.size(),
                    contar(resultados, TipoResultadoFacturacion.GENERADA),
                    contar(resultados, TipoResultadoFacturacion.YA_FACTURADO),
                    facturaRepository.countByLoteId(lote.getId()),
                    resultados.stream()
                            .filter(r -> r.tipo().esIncidencia())
                            .map(r -> new IncidenciaFacturacionResponse(r.numeroContrato(), r.tipo().name(),
                                    r.detalle()))
                            .toList(),
                    sincronizacionErp);
        } catch (RuntimeException ex) {
            gestorLote.marcarError(lote.getId());
            throw ex;
        }
    }

    /** Un contrato con error no detiene el lote: se registra como incidencia. */
    private ResultadoFacturacionContrato facturarAislado(Integer contratoId, Integer loteId) {
        try {
            return facturador.facturar(contratoId, loteId);
        } catch (RuntimeException ex) {
            log.error("Error al facturar el contrato {}", contratoId, ex);
            return new ResultadoFacturacionContrato(contratoId, "id:" + contratoId, TipoResultadoFacturacion.ERROR,
                    null, ex.getMessage());
        }
    }

    private static int contar(List<ResultadoFacturacionContrato> resultados, TipoResultadoFacturacion tipo) {
        return (int) resultados.stream().filter(r -> r.tipo() == tipo).count();
    }
}
