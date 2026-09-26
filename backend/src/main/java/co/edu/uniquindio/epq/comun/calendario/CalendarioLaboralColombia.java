package co.edu.uniquindio.epq.comun.calendario;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.stereotype.Component;

/**
 * Calendario laboral colombiano: días hábiles de lunes a viernes, excluyendo festivos nacionales.
 */
@Component
public class CalendarioLaboralColombia implements CalendarioLaboral {

    private final FestivosColombia festivos = new FestivosColombia();

    @Override
    public boolean esDiaHabil(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY && !festivos.esFestivo(fecha);
    }

    @Override
    public LocalDate sumarDiasHabiles(LocalDate fechaInicio, int dias) {
        if (dias < 0) {
            throw new IllegalArgumentException("La cantidad de días hábiles no puede ser negativa");
        }
        LocalDate fecha = fechaInicio;
        int contados = 0;
        while (contados < dias) {
            fecha = fecha.plusDays(1);
            if (esDiaHabil(fecha)) {
                contados++;
            }
        }
        return fecha;
    }

    @Override
    public LocalDate primerDiaHabil(YearMonth mes) {
        LocalDate fecha = mes.atDay(1);
        while (!esDiaHabil(fecha)) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }
}
