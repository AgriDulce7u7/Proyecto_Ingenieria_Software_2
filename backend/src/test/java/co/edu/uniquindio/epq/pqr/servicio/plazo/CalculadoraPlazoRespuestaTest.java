package co.edu.uniquindio.epq.pqr.servicio.plazo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

    // ------------------------------------------------------------------ corrección entrega 2:
    // el plazo se cuenta en días HÁBILES (lunes a viernes sin festivos), no en días calendario.

    @Test
    void elPlazoNoCuentaFinesDeSemanaComoDiasCalendario() {
        LocalDate fecha = calculadora.calcular(TipoSolicitudTipo.RECLAMO, recepcion).fechaEstimada();
        // 15 días calendario darían el sábado 10 oct; en días hábiles es el lunes 19 oct
        assertNotEquals(recepcion.toLocalDate().plusDays(15), fecha);
        assertEquals(15, contarDiasHabilesEntre(recepcion.toLocalDate(), fecha));
    }

    @Test
    void unaPqrRadicadaEnFinDeSemanaEmpiezaAContarElLunes() {
        LocalDateTime sabado = LocalDateTime.of(2026, 9, 26, 11, 0);
        assertEquals(LocalDate.of(2026, 10, 19),
                calculadora.calcular(TipoSolicitudTipo.QUEJA, sabado).fechaEstimada());
    }

    @Test
    void saltaSemanaSantaYFestivosDeFinDeAnio() {
        // Petición del viernes 27 mar 2026: se saltan Jueves y Viernes Santo (2 y 3 abr)
        assertEquals(LocalDate.of(2026, 4, 14),
                calculadora.calcular(TipoSolicitudTipo.PETICION, LocalDateTime.of(2026, 3, 27, 9, 0)).fechaEstimada());
        // Reclamo del 18 dic 2026: se saltan 25 dic, 1 ene y Reyes (11 ene 2027)
        assertEquals(LocalDate.of(2027, 1, 13),
                calculadora.calcular(TipoSolicitudTipo.RECLAMO, LocalDateTime.of(2026, 12, 18, 9, 0)).fechaEstimada());
    }

    private static int contarDiasHabilesEntre(LocalDate desdeExclusivo, LocalDate hastaInclusivo) {
        CalendarioLaboralColombia calendario = new CalendarioLaboralColombia();
        int dias = 0;
        for (LocalDate d = desdeExclusivo.plusDays(1); !d.isAfter(hastaInclusivo); d = d.plusDays(1)) {
            if (calendario.esDiaHabil(d)) {
                dias++;
            }
        }
        return dias;
    }
}
