package co.edu.uniquindio.epq.comun.persistencia;

import java.time.YearMonth;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte columnas {@code CHAR(7)} con formato 'YYYY-MM' a {@link YearMonth}.
 */
@Converter
public class PeriodoConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth periodo) {
        return periodo == null ? null : periodo.toString();
    }

    @Override
    public YearMonth convertToEntityAttribute(String valor) {
        return valor == null ? null : YearMonth.parse(valor.trim());
    }
}
