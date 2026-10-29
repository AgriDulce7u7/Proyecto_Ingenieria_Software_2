package co.edu.uniquindio.epq.pagos.servicio;

import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.FECHA_VENCIMIENTO;
import static co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion.NUMERO_FACTURA;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.secuencia.GeneradorConsecutivos;
import co.edu.uniquindio.epq.comun.secuencia.SecuenciaNegocio;
import co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoCobroFactura;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import co.edu.uniquindio.epq.facturacion.repositorio.FacturaRepository;
import co.edu.uniquindio.epq.pagos.dominio.CanalPago;
import co.edu.uniquindio.epq.pagos.dominio.Pago;
import co.edu.uniquindio.epq.pagos.evento.PagoRegistradoEvento;
import co.edu.uniquindio.epq.pagos.repositorio.PagoRepository;

/**
 * Pruebas de caracterización de {@link AplicadorPago} (taller de refactorización, hallazgo H-05):
 * fijan el comportamiento observable antes de mover la regla del valor al dominio (H-02).
 */
@DisplayName("AplicadorPago - aplicación de un pago confirmado (F-05)")
class AplicadorPagoTest {

    private static final BigDecimal TOTAL = DatosPruebaFacturacion.LIQUIDACION.total();
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 11, 10, 0);

    private final FacturaRepository facturaRepository = mock(FacturaRepository.class);
    private final PagoRepository pagoRepository = mock(PagoRepository.class);
    private final GeneradorConsecutivos consecutivos = mock(GeneradorConsecutivos.class);
    private final ServicioAuditoria auditoria = mock(ServicioAuditoria.class);
    private final ApplicationEventPublisher eventos = mock(ApplicationEventPublisher.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-11T15:00:00Z"), ZoneId.of("America/Bogota"));

    private final AplicadorPago aplicador =
            new AplicadorPago(facturaRepository, pagoRepository, consecutivos, auditoria, eventos, clock);

    private Factura factura;

    @BeforeEach
    void configurar() {
        factura = DatosPruebaFacturacion.factura();
        when(facturaRepository.buscarParaActualizar(NUMERO_FACTURA)).thenReturn(Optional.of(factura));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(consecutivos.siguiente(SecuenciaNegocio.PAGO)).thenReturn(15L);
        when(consecutivos.siguiente(SecuenciaNegocio.COMPROBANTE)).thenReturn(27L);
    }

    @Test
    void unPagoPorElTotalCreaElPagoYDejaLaFacturaPagada() {
        Pago pago = aplicador.aplicar(" " + NUMERO_FACTURA + " ", TOTAL, CanalPago.PRESENCIAL, null, "RC-100", "asesor1");

        assertAll(
                () -> assertEquals("PAG-000000015", pago.getCodigoPago()),
                () -> assertEquals("CP-000000027", pago.getNumeroComprobante()),
                () -> assertEquals(TOTAL, pago.getValor()),
                () -> assertEquals(CanalPago.PRESENCIAL, pago.getCanal()),
                () -> assertEquals("RC-100", pago.getReferencia()),
                () -> assertEquals("asesor1", pago.getRegistradoPor()),
                () -> assertEquals(AHORA, pago.getFechaPago()),
                () -> assertEquals(EstadoCobroFactura.PAGADA, factura.getEstadoCobro()),
                () -> assertEquals(AHORA, factura.getFechaPago()));
        verify(auditoria).registrar(eq(AccionAuditoria.REGISTRAR_PAGO), eq("factura"), eq(NUMERO_FACTURA), anyString());
        verify(eventos).publishEvent(any(PagoRegistradoEvento.class));
    }

    @Test
    void elValorSeComparaPorMontoYNoPorEscala() {
        aplicador.aplicar(NUMERO_FACTURA, new BigDecimal("81783.250"), CanalPago.BANCO, null, null, "asesor1");
        assertEquals(EstadoCobroFactura.PAGADA, factura.getEstadoCobro());
    }

    @Test
    void unValorDistintoDelTotalSeRechazaSinModificarNada() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> aplicador.aplicar(NUMERO_FACTURA, new BigDecimal("50000.00"), CanalPago.PRESENCIAL, null, null, "asesor1"));

        assertTrue(error.getMessage().contains("no coincide con el total"), error.getMessage());
        assertEquals(EstadoCobroFactura.PENDIENTE, factura.getEstadoCobro());
        verify(pagoRepository, never()).save(any());
        verify(eventos, never()).publishEvent(any());
    }

    @Test
    void swr23UnaFacturaPagadaNoAdmiteOtroPago() {
        factura.registrarPago(AHORA);

        assertThrows(ReglaNegocioException.class,
                () -> aplicador.aplicar(NUMERO_FACTURA, TOTAL, CanalPago.PRESENCIAL, null, null, "asesor1"));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void elEstadoSeValidaAntesQueElValor() {
        factura.registrarPago(AHORA);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> aplicador.aplicar(NUMERO_FACTURA, BigDecimal.ONE, CanalPago.PRESENCIAL, null, null, "asesor1"));
        assertTrue(error.getMessage().contains("ya se encuentra pagada"), error.getMessage());
    }

    @Test
    void unaFacturaEnAcuerdoDePagoNoSePagaDirectamente() {
        factura.vencerSiCorresponde(FECHA_VENCIMIENTO.plusDays(1));
        factura.incluirEnAcuerdo(3);

        assertThrows(ReglaNegocioException.class,
                () -> aplicador.aplicar(NUMERO_FACTURA, TOTAL, CanalPago.PRESENCIAL, null, null, "asesor1"));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void unaFacturaInexistenteResponde404() {
        when(facturaRepository.buscarParaActualizar("FAC-NO-EXISTE")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> aplicador.aplicar("FAC-NO-EXISTE", TOTAL, CanalPago.PRESENCIAL, null, null, "asesor1"));
    }

    @Test
    void rn05UnPagoEnLineaExigeCodigoDeTransaccion() {
        assertThrows(IllegalArgumentException.class,
                () -> aplicador.aplicar(NUMERO_FACTURA, TOTAL, CanalPago.EN_LINEA, null, "ref", "ciudadano:1"));
    }
}
