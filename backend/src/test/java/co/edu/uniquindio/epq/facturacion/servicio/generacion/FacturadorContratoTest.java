package co.edu.uniquindio.epq.facturacion.servicio.generacion;

import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.NUMERO_CONTRATO;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.NUMERO_FACTURA;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.PERIODO;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.contrato;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.lectura;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.lote;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.medidor;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.tarifaAcueducto;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.facturacion.FacturacionProperties;
import co.edu.uniquindio.epq.facturacion.dominio.Contrato;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoContrato;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoFactura;
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
import co.edu.uniquindio.epq.facturacion.servicio.calculo.CalculoPorConsumoMedido;
import co.edu.uniquindio.epq.facturacion.servicio.calculo.SelectorEstrategiaCalculo;
import co.edu.uniquindio.epq.facturacion.servicio.numeracion.GeneradorNumeroFactura;

@DisplayName("FacturadorContrato - liquidación de un contrato")
class FacturadorContratoTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-01T06:00:00Z"), ZoneId.of("America/Bogota"));
    private static final LocalDateTime AHORA = LocalDateTime.now(CLOCK);
    private static final Integer CONTRATO_ID = 1;
    private static final Integer LOTE_ID = 5;
    private static final Integer SERVICIO_ID = 1;

    private final ContratoRepository contratoRepository = mock(ContratoRepository.class);
    private final MedidorRepository medidorRepository = mock(MedidorRepository.class);
    private final LecturaMedidorRepository lecturaRepository = mock(LecturaMedidorRepository.class);
    private final TarifaRepository tarifaRepository = mock(TarifaRepository.class);
    private final FacturaRepository facturaRepository = mock(FacturaRepository.class);
    private final LoteFacturacionRepository loteRepository = mock(LoteFacturacionRepository.class);
    private final GeneradorNumeroFactura generadorNumero = mock(GeneradorNumeroFactura.class);

    // El cálculo es lógica pura: se usa la estrategia real para validar la liquidación completa.
    private final SelectorEstrategiaCalculo selector = new SelectorEstrategiaCalculo(List.of(new CalculoPorConsumoMedido()));

    private FacturadorContrato facturador;
    private Contrato contrato;
    private LoteFacturacion lote;
    private Medidor medidor;
    private LecturaMedidor lectura;
    private Tarifa tarifa;

    @BeforeEach
    void configurar() {
        facturador = crear(15);
        contrato = contrato(EstadoContrato.ACTIVO);
        lote = lote();
        medidor = medidor(contrato);
        lectura = lectura();
        tarifa = tarifaAcueducto();

        // Escenario feliz por defecto; cada test rompe solo la condición que le interesa.
        when(contratoRepository.findById(CONTRATO_ID)).thenReturn(Optional.of(contrato));
        when(loteRepository.findById(LOTE_ID)).thenReturn(Optional.of(lote));
        when(facturaRepository.existsByContratoIdAndPeriodo(CONTRATO_ID, PERIODO)).thenReturn(false);
        when(medidorRepository.findFirstByContratoIdAndEstadoOrderByFechaInstalacionDesc(CONTRATO_ID, Medidor.ESTADO_ACTIVO))
                .thenReturn(Optional.of(medidor));
        when(lecturaRepository.findByMedidorIdAndPeriodo(medidor.getId(), PERIODO)).thenReturn(Optional.of(lectura));
        when(tarifaRepository.buscarVigentes(SERVICIO_ID, PERIODO.atEndOfMonth())).thenReturn(List.of(tarifa));
        when(generadorNumero.generar(PERIODO, contrato)).thenReturn(NUMERO_FACTURA);
    }

    private FacturadorContrato crear(int diasParaVencimiento) {
        return new FacturadorContrato(contratoRepository, medidorRepository, lecturaRepository, tarifaRepository,
                facturaRepository, loteRepository, selector, generadorNumero,
                new FacturacionProperties(true, "0 0 1 * * *", diasParaVencimiento, 60), CLOCK);
    }

    private Factura facturaGuardada() {
        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository).save(captor.capture());
        return captor.getValue();
    }

    private void verificarQueNoSeFacturo() {
        verify(facturaRepository, never()).save(any());
        verify(generadorNumero, never()).generar(any(), any());
    }

    // ------------------------------------------------------------------ factura generada

    @Nested
    @DisplayName("factura generada")
    class FacturaGenerada {

        @Test
        @DisplayName("Liquida CT-ACU-0001: 21,50 m³ × $3.215,50 + $12.650 = $81.783,25")
        void liquidaElContrato() {
            ResultadoFacturacionContrato resultado = facturador.facturar(CONTRATO_ID, LOTE_ID);

            Factura factura = facturaGuardada();
            assertAll(
                    () -> assertEquals(NUMERO_FACTURA, factura.getNumeroFactura()),
                    () -> assertEquals(PERIODO, factura.getPeriodo()),
                    () -> assertEquals(EstadoFactura.GENERADA, factura.getEstado()),
                    () -> assertSame(lote, factura.getLote()),
                    () -> assertSame(contrato, factura.getContrato()),
                    () -> assertSame(lectura, factura.getLectura()),
                    () -> assertSame(tarifa, factura.getTarifa()),
                    () -> assertEquals(new BigDecimal("21.50"), factura.getConsumo()),
                    () -> assertEquals(new BigDecimal("69133.25"), factura.getValorConsumo()),
                    () -> assertEquals(new BigDecimal("12650.00"), factura.getCargoFijo()),
                    () -> assertEquals(new BigDecimal("81783.25"), factura.getTotal()));
            assertAll(
                    () -> assertEquals(TipoResultadoFacturacion.GENERADA, resultado.tipo()),
                    () -> assertEquals(NUMERO_CONTRATO, resultado.numeroContrato()),
                    () -> assertEquals(NUMERO_FACTURA, resultado.numeroFactura()),
                    () -> assertEquals("Total a pagar: 81783.25", resultado.detalle()),
                    () -> assertFalse(resultado.tipo().esIncidencia()));
        }

        @Test
        @DisplayName("Fecha de generación = ahora y vencimiento = hoy + 15 días")
        void fechasDeLaFactura() {
            facturador.facturar(CONTRATO_ID, LOTE_ID);

            Factura factura = facturaGuardada();
            assertEquals(AHORA, factura.getFechaGeneracion());
            assertEquals(LocalDate.of(2026, 9, 16), factura.getFechaVencimiento());
        }

        @Test
        @DisplayName("Los días para el vencimiento se toman de la configuración")
        void vencimientoConfigurable() {
            crear(30).facturar(CONTRATO_ID, LOTE_ID);

            assertEquals(LocalDate.of(2026, 10, 1), facturaGuardada().getFechaVencimiento());
        }

        @Test
        @DisplayName("Usa la tarifa vigente al cierre del periodo de consumo (SWR-03)")
        void tarifaAlCierreDelPeriodo() {
            facturador.facturar(CONTRATO_ID, LOTE_ID);

            verify(tarifaRepository).buscarVigentes(SERVICIO_ID, LocalDate.of(2026, 8, 31));
        }

        @Test
        @DisplayName("Con varias tarifas vigentes usa la primera que devuelve el repositorio")
        void usaLaPrimeraTarifaVigente() {
            Tarifa otra = tarifaAcueducto();
            ReflectionTestUtils.setField(otra, "id", 99);
            ReflectionTestUtils.setField(otra, "valorPorUnidad", new BigDecimal("1000.00"));
            when(tarifaRepository.buscarVigentes(SERVICIO_ID, PERIODO.atEndOfMonth())).thenReturn(List.of(tarifa, otra));

            facturador.facturar(CONTRATO_ID, LOTE_ID);

            assertSame(tarifa, facturaGuardada().getTarifa());
        }
    }

    // ------------------------------------------------------------------ contratos que no se facturan

    @Nested
    @DisplayName("contratos que no se facturan")
    class NoSeFactura {

        @ParameterizedTest(name = "Contrato {0} → NO_ACTIVO")
        @EnumSource(value = EstadoContrato.class, names = {"SUSPENDIDO", "INACTIVO"})
        @DisplayName("Contrato no activo: se excluye sin consultar facturas ni medidor")
        void contratoNoActivo(EstadoContrato estado) {
            ReflectionTestUtils.setField(contrato, "estado", estado);

            ResultadoFacturacionContrato resultado = facturador.facturar(CONTRATO_ID, LOTE_ID);

            assertEquals(TipoResultadoFacturacion.NO_ACTIVO, resultado.tipo());
            verify(facturaRepository, never()).existsByContratoIdAndPeriodo(anyInt(), any());
            verificarQueNoSeFacturo();
        }

        @Test
        @DisplayName("Ya facturado en el periodo: no duplica la factura (idempotencia)")
        void yaFacturado() {
            when(facturaRepository.existsByContratoIdAndPeriodo(CONTRATO_ID, PERIODO)).thenReturn(true);

            ResultadoFacturacionContrato resultado = facturador.facturar(CONTRATO_ID, LOTE_ID);

            assertEquals(TipoResultadoFacturacion.YA_FACTURADO, resultado.tipo());
            assertFalse(resultado.tipo().esIncidencia(), "ya facturado no es una incidencia");
            verify(medidorRepository, never()).findFirstByContratoIdAndEstadoOrderByFechaInstalacionDesc(anyInt(), any());
            verificarQueNoSeFacturo();
        }
    }

    // ------------------------------------------------------------------ incidencias

    @Nested
    @DisplayName("incidencias")
    class Incidencias {

        @Test
        @DisplayName("Sin medidor activo: reporta SIN_MEDIDOR")
        void sinMedidor() {
            when(medidorRepository.findFirstByContratoIdAndEstadoOrderByFechaInstalacionDesc(CONTRATO_ID, Medidor.ESTADO_ACTIVO))
                    .thenReturn(Optional.empty());

            ResultadoFacturacionContrato resultado = facturador.facturar(CONTRATO_ID, LOTE_ID);

            assertEquals(TipoResultadoFacturacion.SIN_MEDIDOR, resultado.tipo());
            assertTrue(resultado.tipo().esIncidencia());
            verificarQueNoSeFacturo();
        }

        @Test
        @DisplayName("Sin lectura del periodo (caso CT-ACU-0005): reporta SIN_LECTURA con el medidor y el periodo")
        void sinLectura() {
            when(lecturaRepository.findByMedidorIdAndPeriodo(medidor.getId(), PERIODO)).thenReturn(Optional.empty());

            ResultadoFacturacionContrato resultado = facturador.facturar(CONTRATO_ID, LOTE_ID);

            assertAll(
                    () -> assertEquals(TipoResultadoFacturacion.SIN_LECTURA, resultado.tipo()),
                    () -> assertTrue(resultado.tipo().esIncidencia()),
                    () -> assertNull(resultado.numeroFactura()),
                    () -> assertTrue(resultado.detalle().contains("MED-" + NUMERO_CONTRATO)),
                    () -> assertTrue(resultado.detalle().contains("2026-08")));
            verify(tarifaRepository, never()).buscarVigentes(anyInt(), any());
            verificarQueNoSeFacturo();
        }

        @Test
        @DisplayName("Sin tarifa vigente: reporta SIN_TARIFA con el servicio y la fecha")
        void sinTarifa() {
            when(tarifaRepository.buscarVigentes(SERVICIO_ID, PERIODO.atEndOfMonth())).thenReturn(List.of());

            ResultadoFacturacionContrato resultado = facturador.facturar(CONTRATO_ID, LOTE_ID);

            assertEquals(TipoResultadoFacturacion.SIN_TARIFA, resultado.tipo());
            assertTrue(resultado.detalle().contains("Acueducto"));
            assertTrue(resultado.detalle().contains("2026-08-31"));
            verificarQueNoSeFacturo();
        }
    }

    // ------------------------------------------------------------------ errores

    @Nested
    @DisplayName("recursos inexistentes")
    class RecursosInexistentes {

        @Test
        @DisplayName("Contrato inexistente: lanza RecursoNoEncontradoException")
        void contratoInexistente() {
            when(contratoRepository.findById(CONTRATO_ID)).thenReturn(Optional.empty());

            assertThrows(RecursoNoEncontradoException.class, () -> facturador.facturar(CONTRATO_ID, LOTE_ID));
            verificarQueNoSeFacturo();
        }

        @Test
        @DisplayName("Lote inexistente: lanza RecursoNoEncontradoException")
        void loteInexistente() {
            when(loteRepository.findById(LOTE_ID)).thenReturn(Optional.empty());

            assertThrows(RecursoNoEncontradoException.class, () -> facturador.facturar(CONTRATO_ID, LOTE_ID));
            verificarQueNoSeFacturo();
        }
    }
}