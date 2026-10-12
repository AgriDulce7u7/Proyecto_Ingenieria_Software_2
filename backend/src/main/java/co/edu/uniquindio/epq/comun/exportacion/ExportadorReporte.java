package co.edu.uniquindio.epq.comun.exportacion;

/**
 * Patrón <b>Strategy</b>: convierte un {@link ReporteTabular} a un formato de archivo.
 * Agregar un formato (CSV, ODS...) es crear una nueva implementación (OCP).
 */
public interface ExportadorReporte {

    FormatoExportacion formato();

    byte[] exportar(ReporteTabular reporte);
}
