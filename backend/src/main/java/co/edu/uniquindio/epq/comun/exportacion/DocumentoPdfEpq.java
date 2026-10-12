package co.edu.uniquindio.epq.comun.exportacion;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Patrón <b>Template Method</b> para todos los documentos PDF del sistema (factura, comprobante de
 * pago, reportes). El esqueleto (encabezado institucional, cuerpo, pie con fecha de generación y manejo
 * de errores) es fijo; cada documento concreto solo implementa su cuerpo.
 */
public abstract class DocumentoPdfEpq {

    protected static final Color AZUL_EPQ = new Color(0, 82, 147);
    protected static final Color GRIS_CLARO = new Color(235, 239, 244);
    protected static final Font FUENTE_TITULO = new Font(Font.HELVETICA, 14, Font.BOLD, AZUL_EPQ);
    protected static final Font FUENTE_SUBTITULO = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
    protected static final Font FUENTE_NORMAL = new Font(Font.HELVETICA, 9, Font.NORMAL);
    protected static final Font FUENTE_NEGRITA = new Font(Font.HELVETICA, 9, Font.BOLD);
    protected static final Font FUENTE_ENCABEZADO_TABLA = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    protected static final Font FUENTE_TABLA = new Font(Font.HELVETICA, 8, Font.NORMAL);
    protected static final Font FUENTE_PIE = new Font(Font.HELVETICA, 7, Font.ITALIC, Color.GRAY);

    private final LocalDateTime generadoEn;

    protected DocumentoPdfEpq(LocalDateTime generadoEn) {
        this.generadoEn = generadoEn;
    }

    /** Método plantilla: no se sobrescribe. */
    public final byte[] generar() {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(tamanoPagina(), 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.addTitle(titulo());
            documento.addCreator("SIGCA-EPQ");
            documento.open();
            agregarEncabezado(documento);
            agregarCuerpo(documento);
            agregarPie(documento);
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible generar el documento PDF '" + titulo() + "'", ex);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
        return salida.toByteArray();
    }

    // ------------------------------------------------------------------ pasos variables

    protected abstract String titulo();

    protected String subtitulo() {
        return null;
    }

    protected Rectangle tamanoPagina() {
        return PageSize.LETTER;
    }

    protected abstract void agregarCuerpo(Document documento) throws Exception;

    // ------------------------------------------------------------------ pasos comunes

    protected void agregarEncabezado(Document documento) throws Exception {
        Paragraph empresa = new Paragraph("Empresas Públicas del Quindío S.A. E.S.P.",
                new Font(Font.HELVETICA, 12, Font.BOLD, AZUL_EPQ));
        documento.add(empresa);
        documento.add(new Paragraph("SIGCA-EPQ · Sistema de Información para la Gestión Comercial y Atención al Cliente",
                FUENTE_SUBTITULO));
        Paragraph titulo = new Paragraph(titulo(), FUENTE_TITULO);
        titulo.setSpacingBefore(10);
        documento.add(titulo);
        if (subtitulo() != null) {
            documento.add(new Paragraph(subtitulo(), FUENTE_SUBTITULO));
        }
        Paragraph separador = new Paragraph(" ");
        separador.setSpacingAfter(4);
        documento.add(separador);
    }

    protected void agregarPie(Document documento) throws Exception {
        Paragraph pie = new Paragraph("Documento generado por SIGCA-EPQ el " + FormatosTexto.fechaHora(generadoEn)
                + ". Prototipo académico - Ingeniería de Software II, Universidad del Quindío.", FUENTE_PIE);
        pie.setSpacingBefore(18);
        pie.setAlignment(Element.ALIGN_CENTER);
        documento.add(pie);
    }

    // ------------------------------------------------------------------ utilidades para subclases

    protected static PdfPCell celda(String texto, Font fuente, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto == null ? "" : texto, fuente));
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(4);
        celda.setBorderColor(Color.LIGHT_GRAY);
        return celda;
    }

    protected static PdfPCell celdaEncabezado(String texto) {
        PdfPCell celda = celda(texto, FUENTE_ENCABEZADO_TABLA, Element.ALIGN_CENTER);
        celda.setBackgroundColor(AZUL_EPQ);
        return celda;
    }

    /** Tabla de dos columnas "etiqueta: valor" para bloques de datos. */
    protected static PdfPTable tablaDatos(String[][] pares) throws Exception {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{35, 65});
        for (String[] par : pares) {
            PdfPCell etiqueta = celda(par[0], FUENTE_NEGRITA, Element.ALIGN_LEFT);
            etiqueta.setBackgroundColor(GRIS_CLARO);
            tabla.addCell(etiqueta);
            tabla.addCell(celda(par[1], FUENTE_NORMAL, Element.ALIGN_LEFT));
        }
        tabla.setSpacingAfter(8);
        return tabla;
    }

    protected static Paragraph seccion(String titulo) {
        Paragraph p = new Paragraph(titulo, new Font(Font.HELVETICA, 10, Font.BOLD, AZUL_EPQ));
        p.setSpacingBefore(8);
        p.setSpacingAfter(4);
        return p;
    }

    protected LocalDateTime generadoEn() {
        return generadoEn;
    }
}
