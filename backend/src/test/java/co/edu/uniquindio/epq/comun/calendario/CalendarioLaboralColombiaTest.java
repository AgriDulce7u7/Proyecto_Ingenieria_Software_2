package co.edu.uniquindio.epq.comun.calendario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

class CalendarioLaboralColombiaTest {

    private final CalendarioLaboralColombia calendario = new CalendarioLaboralColombia();
    private final FestivosColombia festivos = new FestivosColombia();

    @Test
    void reconoceFestivosFijosTrasladablesYDePascua2026() {
        assertTrue(festivos.esFestivo(LocalDate.of(2026, 1, 1)));   // Año nuevo (fijo)
        assertTrue(festivos.esFestivo(LocalDate.of(2026, 1, 12)));  // Reyes, trasladado al lunes
        assertTrue(festivos.esFestivo(LocalDate.of(2026, 4, 2)));   // Jueves Santo
        assertTrue(festivos.esFestivo(LocalDate.of(2026, 4, 3)));   // Viernes Santo
        assertTrue(festivos.esFestivo(LocalDate.of(2026, 10, 12))); // Día de la Raza (ya es lunes)
        assertFalse(festivos.esFestivo(LocalDate.of(2026, 1, 6)));  // Reyes no se celebra el martes
        assertEquals(18, festivos.festivosDelAnio(2026).size());
    }

    @Test
    void finesDeSemanaYFestivosNoSonHabiles() {
        assertFalse(calendario.esDiaHabil(LocalDate.of(2026, 9, 26))); // sábado
        assertFalse(calendario.esDiaHabil(LocalDate.of(2026, 9, 27))); // domingo
        assertFalse(calendario.esDiaHabil(LocalDate.of(2026, 8, 7)));  // Batalla de Boyacá
        assertTrue(calendario.esDiaHabil(LocalDate.of(2026, 9, 28)));  // lunes
    }

    @Test
    void primerDiaHabilDelMesSaltaFinesDeSemanaYFestivos() {
        assertEquals(LocalDate.of(2026, 1, 2), calendario.primerDiaHabil(YearMonth.of(2026, 1))); // 1 ene festivo
        assertEquals(LocalDate.of(2026, 8, 3), calendario.primerDiaHabil(YearMonth.of(2026, 8))); // 1 ago sábado
        assertEquals(LocalDate.of(2026, 10, 1), calendario.primerDiaHabil(YearMonth.of(2026, 10)));
        assertTrue(calendario.esPrimerDiaHabilDelMes(LocalDate.of(2026, 8, 3)));
        assertFalse(calendario.esPrimerDiaHabilDelMes(LocalDate.of(2026, 8, 4)));
    }

    @Test
    void sumaDiasHabilesDesdeElDiaSiguiente() {
        // Viernes 25 sep 2026 + 10 días hábiles = viernes 9 oct 2026
        assertEquals(LocalDate.of(2026, 10, 9), calendario.sumarDiasHabiles(LocalDate.of(2026, 9, 25), 10));
        // Cruza el festivo del 12 de octubre
        assertEquals(LocalDate.of(2026, 10, 13), calendario.sumarDiasHabiles(LocalDate.of(2026, 10, 9), 1));
        assertEquals(LocalDate.of(2026, 9, 25), calendario.sumarDiasHabiles(LocalDate.of(2026, 9, 25), 0));
        assertThrows(IllegalArgumentException.class, () -> calendario.sumarDiasHabiles(LocalDate.now(), -1));
    }
}
