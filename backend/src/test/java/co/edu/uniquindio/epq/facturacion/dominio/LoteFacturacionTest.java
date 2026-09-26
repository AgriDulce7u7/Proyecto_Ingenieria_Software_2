package co.edu.uniquindio.epq.facturacion.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;

class LoteFacturacionTest {

    private final LocalDateTime inicio = LocalDateTime.of(2026, 9, 1, 1, 0);

    @Test
    void ciclo_abrirFinalizarYReabrir() {
        LoteFacturacion lote = LoteFacturacion.abrir(YearMonth.of(2026, 8), inicio);
        assertEquals(EstadoLote.EN_PROCESO, lote.getEstado());
        assertNull(lote.duracion());

        lote.finalizar(8, false, inicio.plusMinutes(3));
        assertTrue(lote.estaCompletado());
        assertEquals(8, lote.getTotalFacturasGeneradas());
        assertEquals(Duration.ofMinutes(3), lote.duracion());

        lote.reabrir(inicio.plusDays(1));
        assertEquals(EstadoLote.EN_PROCESO, lote.getEstado());
        assertNull(lote.getFechaFinProceso());
    }

    @Test
    void finalizarConErroresDejaElLoteEnError() {
        LoteFacturacion lote = LoteFacturacion.abrir(YearMonth.of(2026, 8), inicio);
        lote.finalizar(5, true, inicio.plusMinutes(1));
        assertEquals(EstadoLote.ERROR, lote.getEstado());
    }

    @Test
    void noSePuedeReabrirUnLoteEnProceso() {
        LoteFacturacion lote = LoteFacturacion.abrir(YearMonth.of(2026, 8), inicio);
        assertThrows(ReglaNegocioException.class, () -> lote.reabrir(inicio));
    }
}
