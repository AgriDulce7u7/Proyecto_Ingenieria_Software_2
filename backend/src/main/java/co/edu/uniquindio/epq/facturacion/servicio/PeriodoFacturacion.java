package co.edu.uniquindio.epq.facturacion.servicio;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * Utilidad para interpretar periodos {@code YYYY-MM} recibidos por la API.
 */
public final class PeriodoFacturacion {

    private PeriodoFacturacion() {
    }

    public static YearMonth parsear(String periodo) {
        try {
            return YearMonth.parse(periodo.trim());
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException("Periodo inválido '%s'. Use el formato YYYY-MM".formatted(periodo));
        }
    }
}
