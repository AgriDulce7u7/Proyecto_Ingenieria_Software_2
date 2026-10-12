package co.edu.uniquindio.epq.comun.exportacion;

import java.util.Arrays;

/**
 * Formatos de exportación soportados (SWR-29: PDF y Excel).
 */
public enum FormatoExportacion {

    PDF("application/pdf", "pdf"),
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");

    private final String tipoContenido;
    private final String extension;

    FormatoExportacion(String tipoContenido, String extension) {
        this.tipoContenido = tipoContenido;
        this.extension = extension;
    }

    public String tipoContenido() {
        return tipoContenido;
    }

    public String extension() {
        return extension;
    }

    /** Acepta "pdf", "excel" o "xlsx" sin distinguir mayúsculas. */
    public static FormatoExportacion desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return PDF;
        }
        String normalizado = valor.trim().toUpperCase();
        if ("XLSX".equals(normalizado)) {
            return EXCEL;
        }
        return Arrays.stream(values())
                .filter(f -> f.name().equals(normalizado))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Formato no soportado: " + valor + ". Use pdf o excel"));
    }
}
