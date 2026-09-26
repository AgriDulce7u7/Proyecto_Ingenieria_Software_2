package co.edu.uniquindio.epq.facturacion.servicio.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.facturacion.FacturacionProperties;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoContrato;
import co.edu.uniquindio.epq.facturacion.dominio.LoteFacturacion;
import co.edu.uniquindio.epq.facturacion.repositorio.ContratoRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.facturacion.servicio.SincronizacionErpService;
import co.edu.uniquindio.epq.facturacion.servicio.generacion.FacturadorContrato;
import co.edu.uniquindio.epq.facturacion.servicio.generacion.GestorLoteFacturacion;
import co.edu.uniquindio.epq.facturacion.servicio.generacion.ResultadoFacturacionContrato;
import co.edu.uniquindio.epq.facturacion.servicio.generacion.TipoResultadoFacturacion;
import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoGeneracionLoteResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoSincronizacionResponse;

class GeneracionFacturacionServiceImplTest {

    private static final YearMonth AGOSTO = YearMonth.of(2026, 8);
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final ContratoRepository contratos = mock(ContratoRepository.class);
    private final FacturaRepository facturas = mock(FacturaRepository.class);
    private final GestorLoteFacturacion gestorLote = mock(GestorLoteFacturacion.class);
    private final FacturadorContrato facturador = mock(FacturadorContrato.class);
    private final SincronizacionErpService sincronizacion = mock(SincronizacionErpService.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-01T06:00:00Z"), ZONA);

    private GeneracionFacturacionServiceImpl servicio;
    private LoteFacturacion lote;

    @BeforeEach
    void configurar() {
        servicio = new GeneracionFacturacionServiceImpl(contratos, facturas, gestorLote, facturador, sincronizacion,
                new FacturacionProperties(true, "0 0 1 * * *", 15, 60), clock);
        lote = LoteFacturacion.abrir(AGOSTO, LocalDateTime.now(clock));
        when(gestorLote.abrir(AGOSTO)).thenReturn(lote);
        when(sincronizacion.sincronizarLote(anyString()))
                .thenReturn(new ResultadoSincronizacionResponse("2026-08", 1, 1, 0, List.of()));
    }

    @Test
    void periodoPorDefectoEsElMesAnterior() {
        assertEquals(AGOSTO, servicio.periodoPorDefecto());
    }

    @Test
    void noPermiteFacturarPeriodosFuturos() {
        assertThrows(ReglaNegocioException.class, () -> servicio.generarLote(YearMonth.of(2026, 10), "MANUAL"));
        verify(gestorLote, never()).abrir(any());
    }

    @Test
    void unContratoConErrorNoDetieneElLoteYSeReportaComoIncidencia() {
        when(contratos.buscarIdsPorEstado(EstadoContrato.ACTIVO)).thenReturn(List.of(1, 2, 3));
        when(contratos.count()).thenReturn(5L);
        when(facturador.facturar(eq(1), any())).thenReturn(resultado(1, TipoResultadoFacturacion.GENERADA));
        when(facturador.facturar(eq(2), any())).thenReturn(resultado(2, TipoResultadoFacturacion.SIN_LECTURA));
        when(facturador.facturar(eq(3), any())).thenThrow(new IllegalStateException("fallo inesperado"));
        LoteFacturacion finalizado = LoteFacturacion.abrir(AGOSTO, LocalDateTime.now(clock));
        finalizado.finalizar(1, true, LocalDateTime.now(clock));
        when(gestorLote.finalizar(any(), eq(true))).thenReturn(finalizado);
        when(facturas.countByLoteId(any())).thenReturn(1L);

        ResultadoGeneracionLoteResponse resultado = servicio.generarLote(null, "AUTOMATICO");

        assertEquals("2026-08", resultado.periodo());
        assertEquals(3, resultado.contratosActivosEvaluados());
        assertEquals(2, resultado.contratosNoActivosExcluidos());
        assertEquals(1, resultado.facturasGeneradas());
        assertEquals(2, resultado.incidencias().size());
        assertEquals("ERROR", resultado.estadoLote());
        verify(sincronizacion).sincronizarLote("2026-08");
    }

    private static ResultadoFacturacionContrato resultado(int id, TipoResultadoFacturacion tipo) {
        return ResultadoFacturacionContrato.de(id, "CT-" + id, tipo);
    }
}
