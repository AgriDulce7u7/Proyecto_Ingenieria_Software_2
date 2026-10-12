package co.edu.uniquindio.epq.cartera.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;

class CalculadoraMoraTest {

    @Test
    void cuentaSoloLosPeriodosConsecutivosQueTerminanEnElMasReciente() {
        assertEquals(3, CalculadoraMora.periodosConsecutivos(
                List.of(YearMonth.of(2026, 6), YearMonth.of(2026, 7), YearMonth.of(2026, 8))));
        assertEquals(2, CalculadoraMora.periodosConsecutivos(
                List.of(YearMonth.of(2026, 3), YearMonth.of(2026, 7), YearMonth.of(2026, 8))));
        assertEquals(2, CalculadoraMora.periodosConsecutivos(
                List.of(YearMonth.of(2025, 12), YearMonth.of(2026, 1)))); // cruza el cambio de año
        assertEquals(0, CalculadoraMora.periodosConsecutivos(List.of()));
    }

    @Test
    void rnO3CandidatoASuspensionConDosOMasPeriodosYSinAcuerdoVigente() {
        assertTrue(CalculadoraMora.esCandidatoSuspension(2, false, 2));
        assertFalse(CalculadoraMora.esCandidatoSuspension(1, false, 2));
        assertFalse(CalculadoraMora.esCandidatoSuspension(5, true, 2)); // el acuerdo vigente lo protege
    }

    @Test
    void antiguedadYRangoDeMora() {
        LocalDate hoy = LocalDate.of(2026, 10, 11);
        assertEquals(41, CalculadoraMora.antiguedadDias(LocalDate.of(2026, 8, 31), hoy));
        assertEquals(0, CalculadoraMora.antiguedadDias(LocalDate.of(2026, 10, 20), hoy));
        assertEquals("0-30 días", CalculadoraMora.rangoMora(30));
        assertEquals("31-60 días", CalculadoraMora.rangoMora(41));
        assertEquals("61-90 días", CalculadoraMora.rangoMora(90));
        assertEquals("Más de 90 días", CalculadoraMora.rangoMora(91));
    }
}
