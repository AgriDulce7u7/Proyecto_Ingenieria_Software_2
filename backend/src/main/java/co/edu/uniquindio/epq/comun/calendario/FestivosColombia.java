package co.edu.uniquindio.epq.comun.calendario;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calcula los festivos nacionales de Colombia para cualquier año.
 *
 * <p>Considera festivos fijos, festivos trasladables al lunes siguiente (Ley 51 de 1983,
 * "Ley Emiliani") y festivos que dependen del Domingo de Pascua. Los resultados se guardan
 * en caché por año.</p>
 */
public final class FestivosColombia {

    /** Festivos que se celebran siempre en la misma fecha. */
    private static final List<MonthDay> FIJOS = List.of(
            MonthDay.of(1, 1),    // Año Nuevo
            MonthDay.of(5, 1),    // Día del Trabajo
            MonthDay.of(7, 20),   // Independencia
            MonthDay.of(8, 7),    // Batalla de Boyacá
            MonthDay.of(12, 8),   // Inmaculada Concepción
            MonthDay.of(12, 25)); // Navidad

    /** Festivos que se trasladan al lunes siguiente (Ley Emiliani). */
    private static final List<MonthDay> TRASLADABLES = List.of(
            MonthDay.of(1, 6),    // Reyes Magos
            MonthDay.of(3, 19),   // San José
            MonthDay.of(6, 29),   // San Pedro y San Pablo
            MonthDay.of(8, 15),   // Asunción de la Virgen
            MonthDay.of(10, 12),  // Día de la Raza
            MonthDay.of(11, 1),   // Todos los Santos
            MonthDay.of(11, 11)); // Independencia de Cartagena

    private final Map<Integer, Set<LocalDate>> cache = new ConcurrentHashMap<>();

    public boolean esFestivo(LocalDate fecha) {
        return festivosDelAnio(fecha.getYear()).contains(fecha);
    }

    public Set<LocalDate> festivosDelAnio(int anio) {
        return cache.computeIfAbsent(anio, FestivosColombia::calcular);
    }

    private static Set<LocalDate> calcular(int anio) {
        Set<LocalDate> festivos = new TreeSet<>();
        FIJOS.forEach(md -> festivos.add(md.atYear(anio)));
        TRASLADABLES.forEach(md -> festivos.add(siguienteLunes(md.atYear(anio))));

        LocalDate pascua = domingoDePascua(anio);
        festivos.add(pascua.minusDays(3));                  // Jueves Santo
        festivos.add(pascua.minusDays(2));                  // Viernes Santo
        festivos.add(siguienteLunes(pascua.plusDays(39)));  // Ascensión del Señor
        festivos.add(siguienteLunes(pascua.plusDays(60)));  // Corpus Christi
        festivos.add(siguienteLunes(pascua.plusDays(68)));  // Sagrado Corazón
        return Collections.unmodifiableSet(festivos);
    }

    private static LocalDate siguienteLunes(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    /** Algoritmo de Meeus/Jones/Butcher para el calendario gregoriano. */
    static LocalDate domingoDePascua(int anio) {
        int a = anio % 19;
        int b = anio / 100;
        int c = anio % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(anio, mes, dia);
    }
}
