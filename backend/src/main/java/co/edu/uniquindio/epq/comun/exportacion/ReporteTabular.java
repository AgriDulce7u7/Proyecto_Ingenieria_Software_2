package co.edu.uniquindio.epq.comun.exportacion;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Modelo neutro de un reporte tabular: el mismo objeto se exporta a PDF o a Excel sin que el
 * módulo que lo construye conozca los formatos (SRP / OCP).
 *
 * @param resumen pares "etiqueta → valor" que se muestran al final (totales)
 */
public record ReporteTabular(
        String nombreArchivo,
        String titulo,
        String subtitulo,
        LocalDateTime generadoEn,
        String generadoPor,
        List<ColumnaReporte> columnas,
        List<List<Object>> filas,
        Map<String, Object> resumen) {

    public ReporteTabular {
        columnas = List.copyOf(columnas);
        filas = filas.stream().map(fila -> java.util.Collections.unmodifiableList(new java.util.ArrayList<>(fila))).toList();
        resumen = resumen == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(resumen));
        for (List<Object> fila : filas) {
            if (fila.size() != columnas.size()) {
                throw new IllegalArgumentException("Cada fila debe tener " + columnas.size() + " valores");
            }
        }
    }
}
