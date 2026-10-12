package co.edu.uniquindio.epq.comun.exportacion;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * Exporta un {@link ReporteTabular} a PDF horizontal con el formato institucional.
 */
@Component
public class ExportadorPdf implements ExportadorReporte {

    @Override
    public FormatoExportacion formato() {
        return FormatoExportacion.PDF;
    }

    @Override
    public byte[] exportar(ReporteTabular reporte) {
        return new DocumentoReporte(reporte).generar();
    }

    private static final class DocumentoReporte extends DocumentoPdfEpq {

        private final ReporteTabular reporte;

        private DocumentoReporte(ReporteTabular reporte) {
            super(reporte.generadoEn());
            this.reporte = reporte;
        }

        @Override
        protected String titulo() {
            return reporte.titulo();
        }

        @Override
        protected String subtitulo() {
            String subtitulo = reporte.subtitulo() == null ? "" : reporte.subtitulo();
            return subtitulo + (reporte.generadoPor() == null ? "" : " · Generado por: " + reporte.generadoPor());
        }

        @Override
        protected Rectangle tamanoPagina() {
            return PageSize.LETTER.rotate();
        }

        @Override
        protected void agregarCuerpo(Document documento) throws Exception {
            List<ColumnaReporte> columnas = reporte.columnas();
            PdfPTable tabla = new PdfPTable(columnas.size());
            tabla.setWidthPercentage(100);
            float[] anchos = new float[columnas.size()];
            for (int i = 0; i < columnas.size(); i++) {
                anchos[i] = Math.max(1, columnas.get(i).anchoRelativo());
                tabla.addCell(celdaEncabezado(columnas.get(i).titulo()));
            }
            tabla.setWidths(anchos);
            tabla.setHeaderRows(1);

            if (reporte.filas().isEmpty()) {
                PdfPCell vacia = celda("Sin registros para los criterios seleccionados", FUENTE_TABLA, Element.ALIGN_CENTER);
                vacia.setColspan(columnas.size());
                tabla.addCell(vacia);
            }
            for (List<Object> fila : reporte.filas()) {
                for (int i = 0; i < columnas.size(); i++) {
                    tabla.addCell(celda(FormatosTexto.texto(fila.get(i)), FUENTE_TABLA, alineacion(columnas.get(i))));
                }
            }
            documento.add(tabla);

            if (!reporte.resumen().isEmpty()) {
                documento.add(seccion("Resumen"));
                String[][] pares = new String[reporte.resumen().size()][];
                int i = 0;
                for (Map.Entry<String, Object> entrada : reporte.resumen().entrySet()) {
                    pares[i++] = new String[]{entrada.getKey(), FormatosTexto.texto(entrada.getValue())};
                }
                documento.add(tablaDatos(pares));
            }
        }

        private static int alineacion(ColumnaReporte columna) {
            return switch (columna.tipo()) {
                case MONEDA, ENTERO -> Element.ALIGN_RIGHT;
                case FECHA, BOOLEANO -> Element.ALIGN_CENTER;
                case TEXTO -> Element.ALIGN_LEFT;
            };
        }
    }
}
