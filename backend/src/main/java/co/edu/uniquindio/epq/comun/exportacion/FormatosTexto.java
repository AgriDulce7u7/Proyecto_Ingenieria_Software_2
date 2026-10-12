package co.edu.uniquindio.epq.comun.exportacion;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatos de presentación en español de Colombia (moneda, fechas) usados en los documentos.
 */
public final class FormatosTexto {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private FormatosTexto() {
    }

    public static String moneda(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.of("es", "CO"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        return "$ " + new DecimalFormat("#,##0.00", simbolos).format(valor);
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FECHA);
    }

    public static String fechaHora(LocalDateTime fecha) {
        return fecha == null ? "" : fecha.format(FECHA_HORA);
    }

    /** Representación textual de cualquier valor de una celda. */
    public static String texto(Object valor) {
        if (valor == null) {
            return "";
        }
        if (valor instanceof BigDecimal numero) {
            return moneda(numero);
        }
        if (valor instanceof LocalDateTime fechaHora) {
            return fechaHora(fechaHora);
        }
        if (valor instanceof LocalDate fecha) {
            return fecha(fecha);
        }
        if (valor instanceof Boolean booleano) {
            return booleano ? "Sí" : "No";
        }
        return valor.toString();
    }
}
