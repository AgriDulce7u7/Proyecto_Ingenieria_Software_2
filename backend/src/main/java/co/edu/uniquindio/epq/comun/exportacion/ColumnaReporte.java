package co.edu.uniquindio.epq.comun.exportacion;

/**
 * Columna de un reporte tabular.
 *
 * @param titulo      encabezado
 * @param tipo        tipo de dato, define el formato de la celda
 * @param anchoRelativo peso del ancho de la columna (PDF) y ancho aproximado en caracteres (Excel)
 */
public record ColumnaReporte(String titulo, TipoDato tipo, int anchoRelativo) {

    public enum TipoDato { TEXTO, ENTERO, MONEDA, FECHA, BOOLEANO }

    public static ColumnaReporte texto(String titulo, int ancho) {
        return new ColumnaReporte(titulo, TipoDato.TEXTO, ancho);
    }

    public static ColumnaReporte entero(String titulo, int ancho) {
        return new ColumnaReporte(titulo, TipoDato.ENTERO, ancho);
    }

    public static ColumnaReporte moneda(String titulo, int ancho) {
        return new ColumnaReporte(titulo, TipoDato.MONEDA, ancho);
    }

    public static ColumnaReporte fecha(String titulo, int ancho) {
        return new ColumnaReporte(titulo, TipoDato.FECHA, ancho);
    }

    public static ColumnaReporte booleano(String titulo, int ancho) {
        return new ColumnaReporte(titulo, TipoDato.BOOLEANO, ancho);
    }
}
