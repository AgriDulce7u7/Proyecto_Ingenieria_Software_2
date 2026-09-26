package co.edu.uniquindio.epq.pqr.servicio.plazo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.comun.calendario.CalendarioLaboralColombia;
import co.edu.uniquindio.epq.pqr.dominio.PlazoRespuesta;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitudTipo;

class CalculadoraPlazoRespuestaTest {

    private final CalculadoraPlazoRespuesta calculadora = new CalculadoraPlazoRespuesta(
            List.of(new PlazoPeticion(), new PlazoQuejaReclamo()), new CalendarioLaboralColombia());

    private final LocalDateTime recepcion = LocalDateTime.of(2026, 9, 25, 10, 30); // viernes

    @Test
    void peticionTiene10DiasHabiles() {
        PlazoRespuesta plazo = calculadora.calcular(TipoSolicitudTipo.PETICION, recepcion);
        assertEquals(LocalDate.of(2026, 10, 9), plazo.fechaEstimada());
        assertEquals(LocalDateTime.of(2026, 10, 9, 23, 59, 59), plazo.fechaLimite());
    }

    @Test
    void quejaYReclamoTienen15DiasHabilesExcluyendoFestivos() {
        // 15 días hábiles desde el 25 sep, saltando el festivo del 12 oct → 19 oct 2026
        assertEquals(LocalDate.of(2026, 10, 19),
                calculadora.calcular(TipoSolicitudTipo.QUEJA, recepcion).fechaEstimada());
        assertEquals(LocalDate.of(2026, 10, 19),
                calculadora.calcular(TipoSolicitudTipo.RECLAMO, recepcion).fechaEstimada());
    }
}
