package co.edu.uniquindio.epq.pagos.pasarela;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de caracterización de la política CO-02 (timeout por intento y reintentos). Fijan el comportamiento
 * del procesador antes de la refactorización H-03, que solo cambia la excepción con la que informa que la
 * pasarela no respondió.
 */
@DisplayName("ProcesadorTransaccionesPasarela - política de reintentos CO-02")
class ProcesadorTransaccionesPasarelaTest {

    private static final SolicitudCobro SOLICITUD = new SolicitudCobro("FAC-1-abc", new BigDecimal("81783.25"),
            new DatosTarjeta("4111111111111111", "Carlos Restrepo", 12, 2030, "123"), "EPQ factura FAC-1");

    private final PasarelaPagosGateway gateway = mock(PasarelaPagosGateway.class);
    private ProcesadorTransaccionesPasarela procesador = procesador(1, 2);

    private ProcesadorTransaccionesPasarela procesador(int timeoutSegundos, int maxIntentos) {
        return new ProcesadorTransaccionesPasarela(gateway,
                new PasarelaProperties("simulada", "http://localhost", timeoutSegundos, maxIntentos));
    }

    @AfterEach
    void cerrar() {
        procesador.cerrar();
    }

    @Test
    void unaRespuestaAprobadaSeDevuelveEnElPrimerIntento() {
        when(gateway.cobrar(SOLICITUD)).thenReturn(RespuestaPasarela.aprobada("TX-1", "Aprobada"));

        ProcesadorTransaccionesPasarela.ResultadoCobro resultado = procesador.cobrar(SOLICITUD);

        assertTrue(resultado.respuesta().aprobada());
        assertEquals("TX-1", resultado.respuesta().codigoTransaccion());
        assertEquals(1, resultado.intentos());
    }

    @Test
    void unRechazoNoSeReintenta() {
        when(gateway.cobrar(SOLICITUD)).thenReturn(RespuestaPasarela.rechazada("51", "Fondos insuficientes"));

        ProcesadorTransaccionesPasarela.ResultadoCobro resultado = procesador.cobrar(SOLICITUD);

        assertFalse(resultado.respuesta().aprobada());
        assertEquals(1, resultado.intentos());
        verify(gateway, times(1)).cobrar(any());
    }

    @Test
    void unaFallaTecnicaSeReintentaConLaMismaReferencia() {
        when(gateway.cobrar(SOLICITUD))
                .thenThrow(new PasarelaNoDisponibleException("Sin conexión"))
                .thenReturn(RespuestaPasarela.aprobada("TX-2", "Aprobada"));

        ProcesadorTransaccionesPasarela.ResultadoCobro resultado = procesador.cobrar(SOLICITUD);

        assertTrue(resultado.respuesta().aprobada());
        assertEquals(2, resultado.intentos());
        verify(gateway, times(2)).cobrar(SOLICITUD);
    }

    @Test
    void unErrorInesperadoNoSeReintenta() {
        when(gateway.cobrar(SOLICITUD)).thenThrow(new IllegalArgumentException("Respuesta ilegible"));

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> procesador.cobrar(SOLICITUD));

        assertTrue(error.getCause() instanceof IllegalArgumentException);
        verify(gateway, times(1)).cobrar(any());
    }

    @Test
    void siLaPasarelaNuncaRespondeSeAgotanLosIntentosYSeFalla() {
        when(gateway.cobrar(SOLICITUD)).thenThrow(new PasarelaNoDisponibleException("Sin conexión"));

        RuntimeException error = assertThrows(RuntimeException.class, () -> procesador.cobrar(SOLICITUD));

        assertTrue(error.getMessage().contains("2 intentos"), error.getMessage());
        verify(gateway, times(2)).cobrar(any());
    }

    @Test
    void h03LaFaltaDeRespuestaSeInformaConUnaExcepcionPropiaYTipada() {
        when(gateway.cobrar(SOLICITUD)).thenThrow(new PasarelaNoDisponibleException("Sin conexión"));

        PasarelaSinRespuestaException error =
                assertThrows(PasarelaSinRespuestaException.class, () -> procesador.cobrar(SOLICITUD));

        assertEquals(2, error.intentos());
    }

    @Test
    void unIntentoQueSuperaElTimeoutSeAbandona() {
        procesador.cerrar();
        procesador = procesador(1, 1);
        when(gateway.cobrar(SOLICITUD)).thenAnswer(invocacion -> {
            Thread.sleep(3_000);   // más que el timeout de 1 s
            return RespuestaPasarela.aprobada("TX-TARDE", "Aprobada tarde");
        });

        long inicio = System.nanoTime();
        assertThrows(RuntimeException.class, () -> procesador.cobrar(SOLICITUD));
        long milisegundos = (System.nanoTime() - inicio) / 1_000_000;

        assertTrue(milisegundos < 2_500, "No esperó la respuesta tardía: " + milisegundos + " ms");
    }

    @Test
    void conMaxIntentosEnCeroSeHaceAlMenosUnIntento() {
        procesador.cerrar();
        procesador = procesador(1, 0);
        when(gateway.cobrar(SOLICITUD)).thenReturn(RespuestaPasarela.aprobada("TX-3", "Aprobada"));

        assertEquals(1, procesador.cobrar(SOLICITUD).intentos());
    }
}
