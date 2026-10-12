package co.edu.uniquindio.epq.comun.exportacion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * Exporta un {@link ReporteTabular} a Excel (.xlsx) con encabezados, formatos de moneda/fecha y totales.
 */
@Component
public class ExportadorExcel implements ExportadorReporte {

    @Override
    public FormatoExportacion formato() {
        return FormatoExportacion.EXCEL;
    }

    @Override
    public byte[] exportar(ReporteTabular reporte) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(libro);
            Sheet hoja = libro.createSheet("Reporte");
            int fila = 0;

            Row titulo = hoja.createRow(fila++);
            escribir(titulo.createCell(0), reporte.titulo(), estilos.titulo);
            if (reporte.subtitulo() != null) {
                escribir(hoja.createRow(fila++).createCell(0), reporte.subtitulo(), null);
            }
            escribir(hoja.createRow(fila++).createCell(0),
                    "Generado el " + FormatosTexto.fechaHora(reporte.generadoEn())
                            + (reporte.generadoPor() == null ? "" : " por " + reporte.generadoPor()), null);
            fila++;

            List<ColumnaReporte> columnas = reporte.columnas();
            Row encabezado = hoja.createRow(fila++);
            for (int c = 0; c < columnas.size(); c++) {
                escribir(encabezado.createCell(c), columnas.get(c).titulo(), estilos.encabezado);
                // El ancho se fija explícitamente: autoSizeColumn requiere fuentes del sistema (falla en contenedores)
                hoja.setColumnWidth(c, Math.min(255, Math.max(10, columnas.get(c).anchoRelativo() * 2)) * 256);
            }
            hoja.createFreezePane(0, fila);

            for (List<Object> valores : reporte.filas()) {
                Row r = hoja.createRow(fila++);
                for (int c = 0; c < columnas.size(); c++) {
                    escribir(r.createCell(c), valores.get(c), estilos.para(columnas.get(c).tipo()));
                }
            }

            if (!reporte.resumen().isEmpty()) {
                fila++;
                for (Map.Entry<String, Object> entrada : reporte.resumen().entrySet()) {
                    Row r = hoja.createRow(fila++);
                    escribir(r.createCell(0), entrada.getKey(), estilos.negrita);
                    Object valor = entrada.getValue();
                    escribir(r.createCell(1), valor, valor instanceof BigDecimal ? estilos.moneda : null);
                }
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible generar el archivo de Excel", ex);
        }
    }

    private static void escribir(Cell celda, Object valor, CellStyle estilo) {
        if (valor == null) {
            celda.setCellValue("");
        } else if (valor instanceof BigDecimal numero) {
            celda.setCellValue(numero.doubleValue());
        } else if (valor instanceof Number numero) {
            celda.setCellValue(numero.doubleValue());
        } else if (valor instanceof LocalDateTime fechaHora) {
            celda.setCellValue(fechaHora);
        } else if (valor instanceof LocalDate fecha) {
            celda.setCellValue(fecha);
        } else if (valor instanceof Boolean booleano) {
            celda.setCellValue(booleano ? "Sí" : "No");
        } else {
            celda.setCellValue(valor.toString());
        }
        if (estilo != null) {
            celda.setCellStyle(estilo);
        }
    }

    /** Estilos reutilizados (crear un estilo por celda agota el límite de estilos del libro). */
    private static final class Estilos {
        private final CellStyle titulo;
        private final CellStyle encabezado;
        private final CellStyle negrita;
        private final CellStyle moneda;
        private final CellStyle fecha;
        private final CellStyle entero;

        private Estilos(Workbook libro) {
            Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            titulo = libro.createCellStyle();
            titulo.setFont(fuenteTitulo);

            Font fuenteNegrita = libro.createFont();
            fuenteNegrita.setBold(true);
            negrita = libro.createCellStyle();
            negrita.setFont(fuenteNegrita);

            encabezado = libro.createCellStyle();
            encabezado.setFont(fuenteNegrita);
            encabezado.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            moneda = libro.createCellStyle();
            moneda.setDataFormat(libro.createDataFormat().getFormat("\"$\" #,##0.00"));
            fecha = libro.createCellStyle();
            fecha.setDataFormat(libro.createDataFormat().getFormat("dd/mm/yyyy"));
            entero = libro.createCellStyle();
            entero.setDataFormat(libro.createDataFormat().getFormat("0"));
        }

        private CellStyle para(ColumnaReporte.TipoDato tipo) {
            return switch (tipo) {
                case MONEDA -> moneda;
                case FECHA -> fecha;
                case ENTERO -> entero;
                case TEXTO, BOOLEANO -> null;
            };
        }
    }
}
