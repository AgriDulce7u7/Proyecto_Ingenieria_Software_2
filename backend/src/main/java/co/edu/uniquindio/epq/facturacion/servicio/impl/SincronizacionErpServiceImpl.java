package co.edu.uniquindio.epq.facturacion.servicio.impl;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoFactura;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.dominio.LoteFacturacion;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.LoteFacturacionRepository;
import co.edu.uniquindio.epq.facturacion.servicio.PeriodoFacturacion;
import co.edu.uniquindio.epq.facturacion.servicio.SincronizacionErpService;
import co.edu.uniquindio.epq.facturacion.servicio.sincronizacion.ResultadoSincronizacionFactura;
import co.edu.uniquindio.epq.facturacion.servicio.sincronizacion.SincronizadorFacturaErp;
import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoSincronizacionResponse;

/**
 * Orquesta la sincronización con el ERP. No es transaccional: cada factura se confirma de forma
 * independiente en {@link SincronizadorFacturaErp}, así un fallo no afecta a las demás.
 */
@Service
public class SincronizacionErpServiceImpl implements SincronizacionErpService {

    private static final Logger log = LoggerFactory.getLogger(SincronizacionErpServiceImpl.class);
    private static final EnumSet<EstadoFactura> PENDIENTES =
            EnumSet.of(EstadoFactura.GENERADA, EstadoFactura.ERROR_SINCRONIZACION);

    private final LoteFacturacionRepository loteRepository;
    private final FacturaRepository facturaRepository;
    private final SincronizadorFacturaErp sincronizador;

    public SincronizacionErpServiceImpl(LoteFacturacionRepository loteRepository,
                                        FacturaRepository facturaRepository,
                                        SincronizadorFacturaErp sincronizador) {
        this.loteRepository = loteRepository;
        this.facturaRepository = facturaRepository;
        this.sincronizador = sincronizador;
    }

    @Override
    public ResultadoSincronizacionResponse sincronizarLote(String periodo) {
        YearMonth mes = PeriodoFacturacion.parsear(periodo);
        LoteFacturacion lote = loteRepository.findByPeriodo(mes)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lote de facturación", mes));

        List<Long> pendientes = facturaRepository.buscarIdsPorLoteYEstados(lote.getId(), PENDIENTES);
        int exitosas = 0;
        List<String> conError = new ArrayList<>();
        for (Long facturaId : pendientes) {
            try {
                ResultadoSincronizacionFactura resultado = sincronizador.sincronizar(facturaId);
                if (resultado.exitosa()) {
                    exitosas++;
                } else {
                    conError.add(resultado.numeroFactura());
                }
            } catch (RuntimeException ex) {
                log.error("No fue posible sincronizar la factura con id {}", facturaId, ex);
                conError.add("id:" + facturaId);
            }
        }
        return new ResultadoSincronizacionResponse(mes.toString(), pendientes.size(), exitosas, conError.size(),
                conError);
    }

    @Override
    public ResultadoSincronizacionFactura sincronizarFactura(String numeroFactura) {
        Factura factura = facturaRepository.findByNumeroFactura(numeroFactura)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura", numeroFactura));
        return sincronizador.sincronizar(factura.getId());
    }
}
