package co.edu.uniquindio.epq.facturacion.servicio.impl;

import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.FECHA_GENERACION;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.NUMERO_CONTRATO;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.NUMERO_FACTURA;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.PERIODO;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.contrato;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.factura;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.lote;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import co.edu.uniquindio.epq.comun.calendario.CalendarioLaboral;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.facturacion.FacturacionProperties;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoContrato;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoFactura;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoSincronizacion;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.dominio.LoteFacturacion;
import co.edu.uniquindio.epq.facturacion.dominio.SincronizacionErp;
import co.edu.uniquindio.epq.facturacion.repositorio.ContratoRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.LoteFacturacionRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.SincronizacionErpRepository;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaDetalleResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaResumenResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.LoteFacturacionResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.ProgramacionFacturacionResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.SincronizacionErpResponse;

@DisplayName("ConsultaFacturacionServiceImpl - consultas de facturación")
class ConsultaFacturacionServiceImplTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final LoteFacturacionRepository loteRepository = mock(LoteFacturacionRepository.class);
    private final FacturaRepository facturaRepository = mock(FacturaRepository.class);
    private final SincronizacionErpRepository sincronizacionRepository = mock(SincronizacionErpRepository.class);
    private final ContratoRepository contratoRepository = mock(ContratoRepository.class);
    private final CalendarioLaboral calendario = mock(CalendarioLaboral.class);

    private final ConsultaFacturacionServiceImpl servicio = crear(LocalDate.of(2026, 9, 26));

    private ConsultaFacturacionServiceImpl crear(LocalDate hoy) {
        Clock clock = Clock.fixed(hoy.atStartOfDay(ZONA).plusHours(10).toInstant(), ZONA);
        return new ConsultaFacturacionServiceImpl(loteRepository, facturaRepository, sincronizacionRepository,
                contratoRepository, calendario, new FacturacionProperties(true, "0 0 1 * * *", 15, 60), clock);
    }

    // ------------------------------------------------------------------ programación (SWR-01)

    @Nested
    @DisplayName("obtenerProgramacion (SWR-01)")
    class Programacion {

        // Noviembre de 2026: el 1 es domingo y el 2 es festivo (Todos los Santos) → primer día hábil el 3.
        private final YearMonth noviembre = YearMonth.of(2026, 11);
        private final LocalDate primerHabilNoviembre = LocalDate.of(2026, 11, 3);

        @Test
        @DisplayName("Antes del primer día hábil y sin lote: la próxima ejecución es el primer día hábil")
        void antesDelPrimerDiaHabil() {
            when(calendario.primerDiaHabil(noviembre)).thenReturn(primerHabilNoviembre);

            ProgramacionFacturacionResponse p = crear(LocalDate.of(2026, 11, 1)).obtenerProgramacion();

            assertAll(
                    () -> assertEquals(LocalDate.of(2026, 11, 1), p.hoy()),
                    () -> assertFalse(p.hoyEsPrimerDiaHabil()),
                    () -> assertEquals(primerHabilNoviembre, p.primerDiaHabilMesActual()),
                    () -> assertEquals(primerHabilNoviembre, p.proximaEjecucion()),
                    () -> assertEquals("2026-10", p.periodoPendienteDeFacturar()),
                    () -> assertEquals("SIN_EJECUTAR", p.estadoLotePeriodoPendiente()),
                    () -> assertTrue(p.ejecucionAutomaticaHabilitada()),
                    () -> assertEquals(60, p.tiempoMaximoLoteMinutos()),
                    () -> assertEquals(15, p.diasParaVencimiento()));
        }

        @Test
        @DisplayName("El primer día hábil y sin lote: la próxima ejecución es hoy")
        void elPrimerDiaHabil() {
            when(calendario.primerDiaHabil(noviembre)).thenReturn(primerHabilNoviembre);

            ProgramacionFacturacionResponse p = crear(primerHabilNoviembre).obtenerProgramacion();

            assertTrue(p.hoyEsPrimerDiaHabil());
            assertEquals(primerHabilNoviembre, p.proximaEjecucion());
        }

        @Test
        @DisplayName("Lote del mes anterior completado: la próxima ejecución es el primer día hábil del mes siguiente")
        void loteCompletado() {
            LoteFacturacion completado = LoteFacturacion.abrir(YearMonth.of(2026, 10), FECHA_GENERACION);
            completado.finalizar(6, false, FECHA_GENERACION.plusMinutes(2));
            when(calendario.primerDiaHabil(noviembre)).thenReturn(primerHabilNoviembre);
            when(calendario.primerDiaHabil(YearMonth.of(2026, 12))).thenReturn(LocalDate.of(2026, 12, 1));
            when(loteRepository.findByPeriodo(YearMonth.of(2026, 10))).thenReturn(Optional.of(completado));

            ProgramacionFacturacionResponse p = crear(LocalDate.of(2026, 11, 10)).obtenerProgramacion();

            assertEquals(LocalDate.of(2026, 12, 1), p.proximaEjecucion());
            assertEquals("COMPLETADO", p.estadoLotePeriodoPendiente());
        }

        @Test
        @DisplayName("Lote del mes anterior sin completar: la próxima ejecución es hoy (recupera la ejecución fallida)")
        void loteSinCompletar() {
            LoteFacturacion conError = LoteFacturacion.abrir(YearMonth.of(2026, 10), FECHA_GENERACION);
            conError.marcarError(FECHA_GENERACION.plusMinutes(1));
            when(calendario.primerDiaHabil(noviembre)).thenReturn(primerHabilNoviembre);
            when(loteRepository.findByPeriodo(YearMonth.of(2026, 10))).thenReturn(Optional.of(conError));

            ProgramacionFacturacionResponse p = crear(LocalDate.of(2026, 11, 10)).obtenerProgramacion();

            assertEquals(LocalDate.of(2026, 11, 10), p.proximaEjecucion());
            assertEquals("ERROR", p.estadoLotePeriodoPendiente());
        }
    }

    // ------------------------------------------------------------------ lotes

    @Nested
    @DisplayName("lotes")
    class Lotes {

        @Test
        @DisplayName("Lote finalizado: incluye duración en segundos, conteos por estado y valor total")
        void loteFinalizado() {
            LoteFacturacion lote = lote();
            lote.finalizar(6, false, FECHA_GENERACION.plusMinutes(3));
            when(loteRepository.findByPeriodo(PERIODO)).thenReturn(Optional.of(lote));
            when(facturaRepository.countByLoteIdAndEstado(lote.getId(), EstadoFactura.SINCRONIZADA)).thenReturn(5L);
            when(facturaRepository.countByLoteIdAndEstado(lote.getId(), EstadoFactura.ERROR_SINCRONIZACION)).thenReturn(1L);
            when(facturaRepository.countByLoteIdAndEstado(lote.getId(), EstadoFactura.GENERADA)).thenReturn(0L);
            when(facturaRepository.sumarTotalPorLote(lote.getId())).thenReturn(new BigDecimal("450000.00"));

            LoteFacturacionResponse r = servicio.obtenerLote("2026-08");

            assertAll(
                    () -> assertEquals("2026-08", r.periodo()),
                    () -> assertEquals("COMPLETADO", r.estado()),
                    () -> assertEquals(180L, r.duracionSegundos()),
                    () -> assertEquals(6, r.totalFacturasGeneradas()),
                    () -> assertEquals(5, r.facturasSincronizadas()),
                    () -> assertEquals(1, r.facturasConErrorSincronizacion()),
                    () -> assertEquals(0, r.facturasPendientesSincronizacion()),
                    () -> assertEquals(new BigDecimal("450000.00"), r.valorTotalFacturado()));
        }

        @Test
        @DisplayName("Lote en proceso: la duración es null")
        void loteEnProceso() {
            when(loteRepository.findAllByOrderByPeriodoDesc()).thenReturn(List.of(lote()));

            List<LoteFacturacionResponse> lotes = servicio.listarLotes();

            assertEquals(1, lotes.size());
            assertEquals("EN_PROCESO", lotes.get(0).estado());
            assertNull(lotes.get(0).duracionSegundos());
        }

        @Test
        @DisplayName("Lote inexistente: 404; periodo mal formado: IllegalArgumentException sin consultar la BD")
        void loteInexistenteOInvalido() {
            assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtenerLote("1999-01"));
            assertThrows(IllegalArgumentException.class, () -> servicio.listarFacturasDelLote("enero"));
            verify(loteRepository, times(1)).findByPeriodo(any());
        }

        @Test
        @DisplayName("Facturas del lote: ordenadas por periodo descendente y número ascendente")
        @SuppressWarnings("unchecked")
        void facturasDelLote() {
            when(loteRepository.findByPeriodo(PERIODO)).thenReturn(Optional.of(lote()));
            when(facturaRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(factura()));

            List<FacturaResumenResponse> facturas = servicio.listarFacturasDelLote("2026-08");

            assertEquals(NUMERO_FACTURA, facturas.get(0).numeroFactura());
            ArgumentCaptor<Sort> orden = ArgumentCaptor.forClass(Sort.class);
            verify(facturaRepository).findAll(any(Specification.class), orden.capture());
            assertEquals(Sort.by(Sort.Order.desc("periodo"), Sort.Order.asc("numeroFactura")), orden.getValue());
        }
    }

    // ------------------------------------------------------------------ facturas

    @Nested
    @DisplayName("facturas")
    class Facturas {

        @Test
        @DisplayName("Búsqueda: mapea el resumen de cada factura")
        @SuppressWarnings("unchecked")
        void buscarFacturas() {
            when(facturaRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(factura()));

            List<FacturaResumenResponse> resultado =
                    servicio.buscarFacturas("2026-08", " " + NUMERO_CONTRATO + " ", EstadoFactura.GENERADA, "1094950001");

            FacturaResumenResponse f = resultado.get(0);
            assertAll(
                    () -> assertEquals(NUMERO_FACTURA, f.numeroFactura()),
                    () -> assertEquals(NUMERO_CONTRATO, f.numeroContrato()),
                    () -> assertEquals("Acueducto", f.servicio()),
                    () -> assertEquals(new BigDecimal("81783.25"), f.total()),
                    () -> assertEquals("GENERADA", f.estado()));
        }

        @Test
        @DisplayName("Búsqueda sin filtros o con filtros en blanco: consulta sin fallar")
        @SuppressWarnings("unchecked")
        void buscarSinFiltros() {
            when(facturaRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());

            assertTrue(servicio.buscarFacturas(null, null, null, null).isEmpty());
            assertTrue(servicio.buscarFacturas("  ", "", null, " ").isEmpty());
        }

        @Test
        @DisplayName("Búsqueda con periodo mal formado: IllegalArgumentException sin consultar la BD")
        @SuppressWarnings("unchecked")
        void buscarPeriodoInvalido() {
            assertThrows(IllegalArgumentException.class, () -> servicio.buscarFacturas("2026/08", null, null, null));
            verify(facturaRepository, never()).findAll(any(Specification.class), any(Sort.class));
        }

        @Test
        @DisplayName("Detalle: incluye medidor, lecturas, tarifa y liquidación")
        void obtenerFactura() {
            when(facturaRepository.findByNumeroFactura(NUMERO_FACTURA)).thenReturn(Optional.of(factura()));

            FacturaDetalleResponse d = servicio.obtenerFactura(NUMERO_FACTURA);

            assertAll(
                    () -> assertEquals("MED-" + NUMERO_CONTRATO, d.numeroMedidor()),
                    () -> assertEquals(new BigDecimal("1538.00"), d.lecturaAnterior()),
                    () -> assertEquals(new BigDecimal("1559.50"), d.lecturaActual()),
                    () -> assertEquals("Acueducto residencial 2026", d.tarifa()),
                    () -> assertEquals(new BigDecimal("3215.50"), d.valorPorUnidad()),
                    () -> assertEquals(new BigDecimal("81783.25"), d.total()),
                    () -> assertEquals(NUMERO_CONTRATO, d.contrato().numeroContrato()));
        }

        @Test
        @DisplayName("Factura inexistente: 404")
        void facturaInexistente() {
            assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtenerFactura("FAC-199901-000001"));
            assertThrows(RecursoNoEncontradoException.class,
                    () -> servicio.obtenerSincronizaciones("FAC-199901-000001"));
        }

        @Test
        @DisplayName("Bitácora de sincronizaciones: consulta por el id de la factura")
        void sincronizaciones() {
            Factura factura = factura();
            when(facturaRepository.findByNumeroFactura(NUMERO_FACTURA)).thenReturn(Optional.of(factura));
            when(sincronizacionRepository.findByFacturaIdOrderByFechaSincronizacionDesc(factura.getId()))
                    .thenReturn(List.of(SincronizacionErp.registrar(factura, EstadoSincronizacion.EXITOSO,
                            "Referencia ERP: ERP-1", 2, FECHA_GENERACION)));

            List<SincronizacionErpResponse> bitacora = servicio.obtenerSincronizaciones(NUMERO_FACTURA);

            assertEquals(1, bitacora.size());
            assertEquals("EXITOSO", bitacora.get(0).estado());
            assertEquals(2, bitacora.get(0).intentos());
        }
    }

    // ------------------------------------------------------------------ contratos

    @Nested
    @DisplayName("contratos")
    class Contratos {

        @Test
        @DisplayName("Sin estado: consulta todos los contratos")
        void todos() {
            when(contratoRepository.buscarTodosConDetalle()).thenReturn(List.of(contrato(EstadoContrato.ACTIVO)));

            assertEquals(1, servicio.listarContratos(null).size());
            verify(contratoRepository, never()).buscarPorEstadoConDetalle(any());
        }

        @Test
        @DisplayName("Con estado: filtra por ese estado y mapea los datos del cliente")
        void porEstado() {
            when(contratoRepository.buscarPorEstadoConDetalle(EstadoContrato.SUSPENDIDO))
                    .thenReturn(List.of(contrato(EstadoContrato.SUSPENDIDO)));

            var contratos = servicio.listarContratos(EstadoContrato.SUSPENDIDO);

            assertEquals("SUSPENDIDO", contratos.get(0).estado());
            assertEquals("1094950001", contratos.get(0).numeroDocumentoCliente());
            verify(contratoRepository, never()).buscarTodosConDetalle();
        }
    }
}