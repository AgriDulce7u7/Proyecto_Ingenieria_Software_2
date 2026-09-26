package co.edu.uniquindio.epq.facturacion.servicio.sincronizacion;

import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.DOCUMENTO_CLIENTE;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.FECHA_VENCIMIENTO;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.LIQUIDACION;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.NUMERO_CONTRATO;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.NUMERO_FACTURA;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.factura;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.facturaEn;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.facturacion.ErpProperties;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoFactura;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoSincronizacion;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.dominio.SincronizacionErp;
import co.edu.uniquindio.epq.facturacion.erp.ErpFinancieroGateway;
import co.edu.uniquindio.epq.facturacion.erp.FacturaErp;
import co.edu.uniquindio.epq.facturacion.erp.RespuestaErp;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.facturacion.repositorio.SincronizacionErpRepository;

@DisplayName("SincronizadorFacturaErp - envío de facturas al ERP (SWR-04)")
class SincronizadorFacturaErpTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-01T06:00:00Z"), ZoneId.of("America/Bogota"));
    private static final Long ID_FACTURA = 10L;
    private static final RespuestaErp EXITO = RespuestaErp.exito("ERP-777", "Factura aceptada");
    private static final RespuestaErp RECHAZO = RespuestaErp.fallo("ERP no disponible");

    private final FacturaRepository facturaRepository = mock(FacturaRepository.class);
    private final SincronizacionErpRepository sincronizacionRepository = mock(SincronizacionErpRepository.class);
    private final ErpFinancieroGateway erp = mock(ErpFinancieroGateway.class);

    private SincronizadorFacturaErp sincronizador;
    private Factura factura;

    @BeforeEach
    void configurar() {
        sincronizador = crear(3);
        factura = registrarFactura(factura());
    }

    /** Espera entre intentos en 0 ms para que los tests sean rápidos. */
    private SincronizadorFacturaErp crear(int maxIntentos) {
        ErpProperties propiedades = new ErpProperties("simulado", "http://localhost:9090/api/erp",
                maxIntentos, 0, new ErpProperties.Simulado(0.0));
        return new SincronizadorFacturaErp(facturaRepository, sincronizacionRepository, erp, propiedades, CLOCK);
    }

    private Factura registrarFactura(Factura f) {
        when(facturaRepository.findWithDetalleById(ID_FACTURA)).thenReturn(Optional.of(f));
        return f;
    }

    private SincronizacionErp registroGuardado() {
        ArgumentCaptor<SincronizacionErp> captor = ArgumentCaptor.forClass(SincronizacionErp.class);
        verify(sincronizacionRepository).save(captor.capture());
        return captor.getValue();
    }

    // ------------------------------------------------------------------ validaciones

    @Nested
    @DisplayName("validaciones previas")
    class Validaciones {

        @Test
        @DisplayName("Factura inexistente: lanza RecursoNoEncontradoException sin llamar al ERP")
        void facturaInexistente() {
            when(facturaRepository.findWithDetalleById(99L)).thenReturn(Optional.empty());

            assertThrows(RecursoNoEncontradoException.class, () -> sincronizador.sincronizar(99L));
            verify(erp, never()).enviarFactura(any());
        }

        @ParameterizedTest(name = "Estado {0} → ReglaNegocioException")
        @EnumSource(value = EstadoFactura.class, names = {"SINCRONIZADA", "PAGADA"})
        @DisplayName("No reenvía facturas ya sincronizadas o pagadas")
        void rechazaFacturaQueNoRequiereSincronizacion(EstadoFactura estado) {
            registrarFactura(facturaEn(estado));

            assertThrows(ReglaNegocioException.class, () -> sincronizador.sincronizar(ID_FACTURA));
            verify(erp, never()).enviarFactura(any());
            verify(sincronizacionRepository, never()).save(any());
        }
    }

    // ------------------------------------------------------------------ envío exitoso

    @Nested
    @DisplayName("envío exitoso")
    class EnvioExitoso {

        @Test
        @DisplayName("Primer intento exitoso: marca SINCRONIZADA y registra la bitácora con la referencia del ERP")
        void exitoAlPrimerIntento() {
            when(erp.enviarFactura(any())).thenReturn(EXITO);

            ResultadoSincronizacionFactura resultado = sincronizador.sincronizar(ID_FACTURA);

            assertEquals(EstadoFactura.SINCRONIZADA, factura.getEstado());
            verify(erp, times(1)).enviarFactura(any());

            SincronizacionErp registro = registroGuardado();
            assertAll(
                    () -> assertSame(factura, registro.getFactura()),
                    () -> assertEquals(EstadoSincronizacion.EXITOSO, registro.getEstado()),
                    () -> assertEquals(1, registro.getIntentos()),
                    () -> assertEquals(LocalDateTime.now(CLOCK), registro.getFechaSincronizacion()),
                    () -> assertEquals("Referencia ERP: ERP-777. Factura aceptada", registro.getRespuestaErp()));
            assertAll(
                    () -> assertEquals(NUMERO_FACTURA, resultado.numeroFactura()),
                    () -> assertTrue(resultado.exitosa()),
                    () -> assertEquals(1, resultado.intentos()),
                    () -> assertEquals(registro.getRespuestaErp(), resultado.mensaje()));
        }

        @Test
        @DisplayName("Envía al ERP los datos de la factura en el formato de intercambio")
        void mapeaFacturaAlFormatoErp() {
            when(erp.enviarFactura(any())).thenReturn(EXITO);

            sincronizador.sincronizar(ID_FACTURA);

            ArgumentCaptor<FacturaErp> captor = ArgumentCaptor.forClass(FacturaErp.class);
            verify(erp).enviarFactura(captor.capture());
            FacturaErp enviada = captor.getValue();
            assertAll(
                    () -> assertEquals(NUMERO_FACTURA, enviada.numeroFactura()),
                    () -> assertEquals("2026-08", enviada.periodo()),
                    () -> assertEquals(NUMERO_CONTRATO, enviada.numeroContrato()),
                    () -> assertEquals(DOCUMENTO_CLIENTE, enviada.documentoCliente()),
                    () -> assertEquals("Acueducto", enviada.servicio()),
                    () -> assertEquals(LIQUIDACION.consumo(), enviada.consumo()),
                    () -> assertEquals(LIQUIDACION.subtotal(), enviada.subtotal()),
                    () -> assertEquals(LIQUIDACION.total(), enviada.total()),
                    () -> assertEquals(FECHA_VENCIMIENTO, enviada.fechaVencimiento()));
        }

        @Test
        @DisplayName("Falla y luego éxito: reintenta, queda SINCRONIZADA y registra 2 intentos")
        void exitoTrasReintento() {
            when(erp.enviarFactura(any())).thenReturn(RECHAZO, EXITO);

            ResultadoSincronizacionFactura resultado = sincronizador.sincronizar(ID_FACTURA);

            assertTrue(resultado.exitosa());
            assertEquals(2, resultado.intentos());
            assertEquals(EstadoFactura.SINCRONIZADA, factura.getEstado());
            verify(erp, times(2)).enviarFactura(any());
            assertEquals(2, registroGuardado().getIntentos());
        }

        @Test
        @DisplayName("Factura con error previo: se puede reintentar y queda SINCRONIZADA")
        void reintentaFacturaConErrorPrevio() {
            factura = registrarFactura(facturaEn(EstadoFactura.ERROR_SINCRONIZACION));
            when(erp.enviarFactura(any())).thenReturn(EXITO);

            sincronizador.sincronizar(ID_FACTURA);

            assertEquals(EstadoFactura.SINCRONIZADA, factura.getEstado());
        }
    }

    // ------------------------------------------------------------------ fallos

    @Nested
    @DisplayName("fallos y reintentos")
    class Fallos {

        @Test
        @DisplayName("Todos los intentos fallan: queda ERROR_SINCRONIZACION y se registra el último mensaje")
        void agotaLosIntentos() {
            when(erp.enviarFactura(any())).thenReturn(RECHAZO);

            ResultadoSincronizacionFactura resultado = sincronizador.sincronizar(ID_FACTURA);

            assertFalse(resultado.exitosa());
            assertEquals(3, resultado.intentos());
            assertEquals(EstadoFactura.ERROR_SINCRONIZACION, factura.getEstado());
            verify(erp, times(3)).enviarFactura(any());

            SincronizacionErp registro = registroGuardado();
            assertEquals(EstadoSincronizacion.FALLIDO, registro.getEstado());
            assertEquals(3, registro.getIntentos());
            assertEquals("ERP no disponible", registro.getRespuestaErp());
        }

        @Test
        @DisplayName("Una excepción técnica del ERP cuenta como intento fallido y no interrumpe los reintentos")
        void excepcionTecnicaCuentaComoIntento() {
            when(erp.enviarFactura(any()))
                    .thenThrow(new RuntimeException("Connection refused"))
                    .thenReturn(EXITO);

            ResultadoSincronizacionFactura resultado = sincronizador.sincronizar(ID_FACTURA);

            assertTrue(resultado.exitosa());
            assertEquals(2, resultado.intentos());
        }

        @Test
        @DisplayName("Si todas las llamadas lanzan excepción, registra el error de comunicación")
        void soloExcepciones() {
            when(erp.enviarFactura(any())).thenThrow(new RuntimeException("timeout"));

            ResultadoSincronizacionFactura resultado = sincronizador.sincronizar(ID_FACTURA);

            assertFalse(resultado.exitosa());
            assertEquals(EstadoFactura.ERROR_SINCRONIZACION, factura.getEstado());
            assertEquals("Error de comunicación con el ERP: timeout", registroGuardado().getRespuestaErp());
        }

        @Test
        @DisplayName("El número de intentos se toma de la configuración")
        void intentosConfigurables() {
            when(erp.enviarFactura(any())).thenReturn(RECHAZO);

            ResultadoSincronizacionFactura resultado = crear(5).sincronizar(ID_FACTURA);

            assertEquals(5, resultado.intentos());
            verify(erp, times(5)).enviarFactura(any());
        }

        @Test
        @DisplayName("Con maxIntentos en 0 o negativo, hace al menos un intento")
        void siempreAlMenosUnIntento() {
            when(erp.enviarFactura(any())).thenReturn(RECHAZO);

            ResultadoSincronizacionFactura resultado = crear(0).sincronizar(ID_FACTURA);

            assertEquals(1, resultado.intentos());
            verify(erp, times(1)).enviarFactura(any());
        }
    }
}