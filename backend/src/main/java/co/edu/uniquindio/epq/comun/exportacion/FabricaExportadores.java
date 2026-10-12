package co.edu.uniquindio.epq.comun.exportacion;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Patrón <b>Factory</b>: entrega el exportador del formato solicitado. Spring inyecta todas las
 * estrategias disponibles; no hay {@code switch} por formato.
 */
@Component
public class FabricaExportadores {

    private final Map<FormatoExportacion, ExportadorReporte> exportadores = new EnumMap<>(FormatoExportacion.class);

    public FabricaExportadores(List<ExportadorReporte> disponibles) {
        disponibles.forEach(e -> exportadores.put(e.formato(), e));
    }

    public ArchivoGenerado exportar(ReporteTabular reporte, FormatoExportacion formato) {
        ExportadorReporte exportador = exportadores.get(formato);
        if (exportador == null) {
            throw new IllegalArgumentException("No hay exportador para el formato " + formato);
        }
        return new ArchivoGenerado(reporte.nombreArchivo() + "." + formato.extension(), formato.tipoContenido(),
                exportador.exportar(reporte));
    }
}
