package co.edu.uniquindio.epq.pagos.servicio.impl;

import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.NUMERO_FACTURA;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.comun.excepcion.RecursoDuplicadoException;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.pagos.dominio.CanalPago;
import co.edu.uniquindio.epq.pagos.dominio.IntentoPagoEnLinea;
import co.edu.uniquindio.epq.pagos.dominio.Pago;
import co.edu.uniquindio.epq.pagos.dominio.ResultadoIntentoPago;
import co.edu.uniquindio.epq.pagos.pasarela.PasarelaNoDisponibleException;
import co.edu.uniquindio.epq.pagos.pasarela.PasarelaPagosGateway;
import co.edu.uniquindio.epq.pagos.pasarela.PasarelaProperties;
import co.edu.uniquindio.epq.pagos.pasarela.ProcesadorTransaccionesPasarela;
import co.edu.uniquindio.epq.pagos.pasarela.ProcesadorTransaccionesPasarela.ResultadoCobro;
import co.edu.uniquindio.epq.pagos.pasarela.RespuestaPasarela;
import co.edu.uniquindio.epq.pagos.repositorio.IntentoPagoEnLineaRepository;
import co.edu.uniquindio.epq.pagos.repositorio.PagoRepository;
import co.edu.uniquindio.epq.pagos.servicio.AplicadorPago;
import co.edu.uniquindio.epq.pagos.servicio.PagoRechazadoException;
import co.edu.uniquindio.epq.pagos.servicio.TimeoutPagoException;
import co.edu.uniquindio.epq.pagos.web.dto.ComprobantePagoResponse;
import co.edu.uniquindio.epq.pagos.web.dto.PagoEnLineaRequest;
import co.edu.uniquindio.epq.pagos.web.dto.RegistrarPagoPresencialRequest;
import co.edu.uniquindio.epq.seguridad.Rol;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;
import co.edu.uniquindio.epq.seguridad.contexto.TipoUsuario;
import co.edu.uniquindio.epq.seguridad.contexto.UsuarioAutenticado;

/**
 * Pruebas de caracterización de {@link RegistroPagoServiceImpl} (taller de refactorización).
 * Protegen los dos flujos de F-05 (presencial y en línea) antes de las refactorizaciones H-02 y H-03;
 * deben pasar sin modificaciones antes y después de ambas.
 */
@DisplayName("RegistroPagoServiceImpl - registro de pagos (F-05)")
class RegistroPagoServiceImplTest {

    private static final BigDecimal TOTAL = DatosPruebaFacturacion.LIQUIDACION.total();
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 11, 10, 0);
    private static final String TARJETA_APROBADA = "4111111111111111";

    private final AplicadorPago aplicador = mock(AplicadorPago.class);
    private final FacturaRepository facturaRepository = mock(FacturaRepository.class);
    private final PagoRepository pagoRepository = mock(PagoRepository.class);
    private final IntentoPagoEnLineaRepository intentoRepository = mock(IntentoPagoEnLineaRepository.class);
    private final ProcesadorTransaccionesPasarela pasarela = mock(ProcesadorTransaccionesPasarela.class);
    private final ServicioAuditoria auditoria = mock(ServicioAuditoria.class);
    private final ContextoSeguridad contexto = mock(ContextoSeguridad.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-11T15:00:00Z"), ZoneId.of("America/Bogota"));

    private final RegistroPagoServiceImpl servicio = new RegistroPagoServiceImpl(aplicador, facturaRepository,
            pagoRepository, intentoRepository, pasarela, auditoria, contexto, clock);

    private final UsuarioAutenticado ciudadano = new UsuarioAutenticado(1, DatosPruebaFacturacion.DOCUMENTO_CLIENTE,
            "Carlos Mario Restrepo", Rol.CIUDADANO, TipoUsuario.CIUDADANO, null, null);

    private Factura factura;

    @BeforeEach
    void configurar() {
        factura = DatosPruebaFacturacion.factura();
        when(facturaRepository.findByNumeroFactura(NUMERO_FACTURA)).thenReturn(Optional.of(factura));
        when(contexto.requerirUsuario()).thenReturn(ciudadano);
        when(contexto.identificadorActual()).thenReturn("asesor1");
    }

    private Pago pagoAplicado(CanalPago canal, String codigoTransaccion) {
        return Pago.registrar("PAG-000000001", factura, TOTAL, canal, AHORA, codigoTransaccion, "REF",
                "CP-000000001", "asesor1");
    }

    private static PagoEnLineaRequest tarjeta(BigDecimal valor) {
        return new PagoEnLineaRequest(TARJETA_APROBADA, " Carlos Restrepo ", 12, 2030, "123", valor);
    }

    // ------------------------------------------------------------------ presencial

    @Nested
    @DisplayName("registrarPresencial")
    class RegistrarPresencial {

        @Test
        void rn05ElCanalEnLineaNoSeRegistraManualmente() {
            var solicitud = new RegistrarPagoPresencialRequest(NUMERO_FACTURA, TOTAL, CanalPago.EN_LINEA, null);

            assertThrows(ReglaNegocioException.class, () -> servicio.registrarPresencial(solicitud));
            verify(aplicador, never()).aplicar(any(), any(), any(), any(), any(), any());
        }

        @Test
        void delegaEnElAplicadorConLaReferenciaNormalizada() {
            when(aplicador.aplicar(any(), any(), any(), any(), any(), any()))
                    .thenReturn(pagoAplicado(CanalPago.BANCO, null));

            ComprobantePagoResponse comprobante = servicio.registrarPresencial(
                    new RegistrarPagoPresencialRequest(NUMERO_FACTURA, TOTAL, CanalPago.BANCO, "  CONS-77  "));

            verify(aplicador).aplicar(NUMERO_FACTURA, TOTAL, CanalPago.BANCO, null, "CONS-77", "asesor1");
            assertEquals(NUMERO_FACTURA, comprobante.numeroFactura());
        }

        @Test
        void unaReferenciaEnBlancoSeGuardaComoNula() {
            when(aplicador.aplicar(any(), any(), any(), any(), any(), any()))
                    .thenReturn(pagoAplicado(CanalPago.PRESENCIAL, null));

            servicio.registrarPresencial(new RegistrarPagoPresencialRequest(NUMERO_FACTURA, TOTAL, CanalPago.PRESENCIAL, "   "));

            verify(aplicador).aplicar(eq(NUMERO_FACTURA), eq(TOTAL), eq(CanalPago.PRESENCIAL), isNull(), isNull(), eq("asesor1"));
        }

        @Test
        void lasReglasDeLaFacturaLasAplicaElAplicador() {
            when(aplicador.aplicar(any(), any(), any(), any(), any(), any()))
                    .thenThrow(new ReglaNegocioException("La factura ya se encuentra pagada"));

            assertThrows(ReglaNegocioException.class, () -> servicio.registrarPresencial(
                    new RegistrarPagoPresencialRequest(NUMERO_FACTURA, TOTAL, CanalPago.PRESENCIAL, null)));
        }
    }

    // ------------------------------------------------------------------ en línea

    @Nested
    @DisplayName("pagarEnLinea")
    class PagarEnLinea {

        @Test
        void unaFacturaDeOtroClienteNoExisteParaElCiudadano() {
            UsuarioAutenticado otro = new UsuarioAutenticado(99, "1094000000", "Otra persona", Rol.CIUDADANO,
                    TipoUsuario.CIUDADANO, null, null);
            when(contexto.requerirUsuario()).thenReturn(otro);

            assertThrows(RecursoNoEncontradoException.class, () -> servicio.pagarEnLinea(NUMERO_FACTURA, tarjeta(null)));
            verify(pasarela, never()).cobrar(any());
        }

        @Test
        void swr23UnaFacturaPagadaNoLlegaALaPasarela() {
            factura.registrarPago(AHORA);

            assertThrows(ReglaNegocioException.class, () -> servicio.pagarEnLinea(NUMERO_FACTURA, tarjeta(null)));
            verify(pasarela, never()).cobrar(any());
        }

        @Test
        void co02UnValorDistintoDelTotalSeRechazaAntesDeCobrar() {
            assertThrows(ReglaNegocioException.class,
                    () -> servicio.pagarEnLinea(NUMERO_FACTURA, tarjeta(new BigDecimal("1000.00"))));
            verify(pasarela, never()).cobrar(any());
            verify(intentoRepository, never()).save(any());
        }

        @Test
        void unaTransaccionAprobadaSeAplicaPorElTotalYQuedaElIntento() {
            when(pasarela.cobrar(any())).thenReturn(new ResultadoCobro(RespuestaPasarela.aprobada("TX-1", "Aprobada"), 1));
            Pago pago = pagoAplicado(CanalPago.EN_LINEA, "TX-1");
            when(aplicador.aplicar(any(), any(), any(), any(), any(), any())).thenReturn(pago);
            when(pagoRepository.findWithDetalleById(any())).thenReturn(Optional.of(pago));

            ComprobantePagoResponse comprobante = servicio.pagarEnLinea(NUMERO_FACTURA, tarjeta(TOTAL));

            verify(aplicador).aplicar(eq(NUMERO_FACTURA), eq(TOTAL), eq(CanalPago.EN_LINEA), eq("TX-1"),
                    startsWith(NUMERO_FACTURA + "-"), eq("ciudadano:" + DatosPruebaFacturacion.DOCUMENTO_CLIENTE));
            IntentoPagoEnLinea intento = intentoGuardado();
            assertAll(
                    () -> assertEquals("CP-000000001", comprobante.numeroComprobante()),
                    () -> assertEquals(ResultadoIntentoPago.APROBADO, intento.getResultado()),
                    () -> assertEquals("1111", intento.getUltimosDigitos()),
                    () -> assertEquals(TOTAL, intento.getValor()),
                    () -> assertEquals(AHORA, intento.getFecha()));
        }

        @Test
        void sinValorEnLaSolicitudSeCobraElTotalDeLaFactura() {
            when(pasarela.cobrar(any())).thenReturn(new ResultadoCobro(RespuestaPasarela.aprobada("TX-2", "Aprobada"), 1));
            Pago pago = pagoAplicado(CanalPago.EN_LINEA, "TX-2");
            when(aplicador.aplicar(any(), any(), any(), any(), any(), any())).thenReturn(pago);
            when(pagoRepository.findWithDetalleById(any())).thenReturn(Optional.of(pago));

            servicio.pagarEnLinea(NUMERO_FACTURA, tarjeta(null));

            verify(pasarela).cobrar(argThat(s -> s.monto().compareTo(TOTAL) == 0));
        }

        @Test
        void unRechazoDeLaPasarelaSeRegistraYAuditaSinAplicarElPago() {
            when(pasarela.cobrar(any())).thenReturn(
                    new ResultadoCobro(RespuestaPasarela.rechazada("51", "Fondos insuficientes"), 1));

            assertThrows(PagoRechazadoException.class, () -> servicio.pagarEnLinea(NUMERO_FACTURA, tarjeta(null)));

            assertEquals(ResultadoIntentoPago.RECHAZADO, intentoGuardado().getResultado());
            verify(auditoria).registrarIndependiente(anyString(), eq(Rol.CIUDADANO.name()),
                    eq(AccionAuditoria.PAGO_EN_LINEA_RECHAZADO), eq("factura"), eq(NUMERO_FACTURA), anyString(), anyBoolean());
            verify(aplicador, never()).aplicar(any(), any(), any(), any(), any(), any());
        }

        @Test
        void co02SinRespuestaDeLaPasarelaSeRegistraElIntentoYSePropagaElTimeout() {
            // Procesador REAL con un gateway que nunca responde: la prueba no depende de qué excepción
            // interna use la pasarela, solo del comportamiento observable del servicio (504 + intento).
            PasarelaPagosGateway gatewaySinRespuesta = mock(PasarelaPagosGateway.class);
            when(gatewaySinRespuesta.cobrar(any())).thenThrow(new PasarelaNoDisponibleException("Sin conexión"));
            ProcesadorTransaccionesPasarela procesadorReal = new ProcesadorTransaccionesPasarela(gatewaySinRespuesta,
                    new PasarelaProperties("simulada", "http://localhost", 1, 2));
            RegistroPagoServiceImpl servicioConPasarelaReal = new RegistroPagoServiceImpl(aplicador, facturaRepository,
                    pagoRepository, intentoRepository, procesadorReal, auditoria, contexto, clock);

            TimeoutPagoException error = assertThrows(TimeoutPagoException.class,
                    () -> servicioConPasarelaReal.pagarEnLinea(NUMERO_FACTURA, tarjeta(null)));

            assertEquals(HttpStatus.GATEWAY_TIMEOUT, error.estadoHttp());
            assertEquals(2, error.propiedades().get("intentos"));
            assertTrue(error.getMessage().contains("no respondió después de 2 intentos"), error.getMessage());
            IntentoPagoEnLinea intento = intentoGuardado();
            assertEquals(ResultadoIntentoPago.SIN_RESPUESTA, intento.getResultado());
            assertEquals(2, intento.getIntentos());
            assertEquals(error.getMessage(), intento.getMensaje());
            assertNull(intento.getCodigoTransaccion());
            verify(gatewaySinRespuesta, times(2)).cobrar(any());
            verify(aplicador, never()).aplicar(any(), any(), any(), any(), any(), any());
        }

        @Test
        void unCobroAprobadoQueNoPuedeAplicarseSeInformaParaReverso() {
            when(pasarela.cobrar(any())).thenReturn(new ResultadoCobro(RespuestaPasarela.aprobada("TX-3", "Aprobada"), 1));
            when(aplicador.aplicar(any(), any(), any(), any(), any(), any()))
                    .thenThrow(new ReglaNegocioException("La factura ya se encuentra pagada"));

            ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                    () -> servicio.pagarEnLinea(NUMERO_FACTURA, tarjeta(null)));

            assertTrue(error.getMessage().contains("TX-3"), error.getMessage());
            assertTrue(error.getMessage().contains("reverso"), error.getMessage());
            assertEquals(ResultadoIntentoPago.APROBADO, intentoGuardado().getResultado());
        }

        @Test
        void noSeAdmitenDosPagosEnLineaSimultaneosDeLaMismaFactura() {
            AtomicReference<RuntimeException> errorConcurrente = new AtomicReference<>();
            when(pasarela.cobrar(any())).thenAnswer(invocacion -> {
                try {
                    servicio.pagarEnLinea(NUMERO_FACTURA, tarjeta(null));   // segundo pago mientras el primero está en curso
                } catch (RuntimeException ex) {
                    errorConcurrente.set(ex);
                }
                return new ResultadoCobro(RespuestaPasarela.rechazada("05", "No autorizada"), 1);
            });

            assertThrows(PagoRechazadoException.class, () -> servicio.pagarEnLinea(NUMERO_FACTURA, tarjeta(null)));
            assertInstanceOf(RecursoDuplicadoException.class, errorConcurrente.get());
        }

        @Test
        void alTerminarUnPagoSeLiberaLaFacturaParaUnNuevoIntento() {
            when(pasarela.cobrar(any())).thenReturn(new ResultadoCobro(RespuestaPasarela.rechazada("05", "No autorizada"), 1));

            assertThrows(PagoRechazadoException.class, () -> servicio.pagarEnLinea(NUMERO_FACTURA, tarjeta(null)));
            assertThrows(PagoRechazadoException.class, () -> servicio.pagarEnLinea(NUMERO_FACTURA, tarjeta(null)));

            verify(pasarela, times(2)).cobrar(any());
        }

        private IntentoPagoEnLinea intentoGuardado() {
            ArgumentCaptor<IntentoPagoEnLinea> captor = ArgumentCaptor.forClass(IntentoPagoEnLinea.class);
            verify(intentoRepository).save(captor.capture());
            return captor.getValue();
        }
    }
}
