package co.edu.uniquindio.epq.pagos.pasarela;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class PasarelaPagosSimuladaAdapterTest {

    private final PasarelaPagosSimuladaAdapter pasarela = new PasarelaPagosSimuladaAdapter(
            Clock.fixed(Instant.parse("2026-10-11T15:00:00Z"), ZoneId.of("America/Bogota")));

    @Test
    void validaElNumeroDeTarjetaConLuhn() {
        assertTrue(PasarelaPagosSimuladaAdapter.cumpleLuhn("4111111111111111"));
        assertFalse(PasarelaPagosSimuladaAdapter.cumpleLuhn("4111111111111112"));
        assertFalse(PasarelaPagosSimuladaAdapter.cumpleLuhn("41111111111a1111"));
        assertFalse(PasarelaPagosSimuladaAdapter.cumpleLuhn("4242")); // muy corta
    }

    @Test
    void apruebaLaTarjetaDePrueba() {
        RespuestaPasarela r = pasarela.cobrar(solicitud("4111111111111111", 2028));
        assertTrue(r.aprobada());
        assertTrue(r.codigoTransaccion().startsWith("TX-"));
    }

    @Test
    void rechazaTarjetaVencidaYNumeroInvalido() {
        assertEquals("54", pasarela.cobrar(solicitud("4111111111111111", 2025)).codigoRespuesta());
        assertEquals("14", pasarela.cobrar(solicitud("4111111111111112", 2028)).codigoRespuesta());
    }

    @Test
    void simulaPasarelaSinRespuesta() {
        assertThrows(PasarelaNoDisponibleException.class,
                () -> pasarela.cobrar(solicitud("4000000000000119", 2028)));
    }

    @Test
    void elToStringDeLaTarjetaNoExponeElNumeroNiElCvv() {
        DatosTarjeta tarjeta = new DatosTarjeta("4111111111111111", "ANA PEREZ", 12, 2028, "123");
        assertFalse(tarjeta.toString().contains("4111111111111111"));
        assertFalse(tarjeta.toString().contains("123"));
        assertEquals("1111", tarjeta.ultimosDigitos());
    }

    private static SolicitudCobro solicitud(String numero, int anio) {
        return new SolicitudCobro("FAC-202609-000001", new BigDecimal("81783.25"),
                new DatosTarjeta(numero, "ANA PEREZ", 12, anio, "123"), "Pago factura");
    }
}
