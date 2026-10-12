package co.edu.uniquindio.epq.facturacion.documento;

import java.time.LocalDateTime;
import java.util.List;

import co.edu.uniquindio.epq.comercial.dominio.Contrato;
import co.edu.uniquindio.epq.comun.exportacion.DocumentoPdfEpq;
import co.edu.uniquindio.epq.comun.exportacion.FormatosTexto;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * Factura de servicios públicos en el formato de EPQ (SWR-27): datos del usuario y del predio,
 * lecturas, consumo, liquidación, total a pagar, vencimiento y los últimos consumos.
 */
public class FacturaPdf extends DocumentoPdfEpq {

    private final Factura factura;
    private final List<Factura> historial;

    public FacturaPdf(Factura factura, List<Factura> historialReciente, LocalDateTime generadoEn) {
        super(generadoEn);
        this.factura = factura;
        this.historial = historialReciente;
    }

    @Override
    protected String titulo() {
        return "Factura de servicios públicos N.° " + factura.getNumeroFactura();
    }

    @Override
    protected String subtitulo() {
        return "Servicio de " + factura.getContrato().getServicio().getNombre() + " · Período facturado "
                + factura.getPeriodo();
    }

    @Override
    protected void agregarCuerpo(Document documento) throws Exception {
        Contrato contrato = factura.getContrato();
        documento.add(seccion("Datos del usuario y del servicio"));
        documento.add(tablaDatos(new String[][]{
                {"Usuario", contrato.getCliente().getNombreCompleto()},
                {"Identificación", contrato.getCliente().getTipoDocumento() + " " + contrato.getCliente().getNumeroDocumento()},
                {"Contrato", contrato.getNumeroContrato()},
                {"Dirección del servicio", contrato.getDireccionServicio()},
                {"Municipio / estrato", contrato.getPredio() == null ? "-"
                        : contrato.getPredio().getMunicipio().getNombre() + " / estrato " + contrato.getPredio().getEstrato()},
                {"Medidor", factura.getLectura().getMedidor().getNumeroSerie()}
        }));

        documento.add(seccion("Lectura y consumo"));
        documento.add(tablaDatos(new String[][]{
                {"Período", factura.getPeriodo().toString()},
                {"Lectura anterior", factura.getLectura().getLecturaAnterior().toPlainString()},
                {"Lectura actual", factura.getLectura().getLecturaActual().toPlainString()},
                {"Consumo (m³)", factura.getConsumo().toPlainString()}
        }));

        documento.add(seccion("Liquidación"));
        PdfPTable liquidacion = new PdfPTable(2);
        liquidacion.setWidthPercentage(100);
        liquidacion.setWidths(new float[]{70, 30});
        agregarFila(liquidacion, "Tarifa aplicada: " + factura.getTarifa().getNombre() + " ("
                + FormatosTexto.moneda(factura.getTarifa().getValorPorUnidad()) + " por m³)", "");
        agregarFila(liquidacion, "Valor del consumo", FormatosTexto.moneda(factura.getValorConsumo()));
        agregarFila(liquidacion, "Cargo fijo", FormatosTexto.moneda(factura.getCargoFijo()));
        agregarFila(liquidacion, "Subtotal", FormatosTexto.moneda(factura.getSubtotal()));
        PdfPCell etiquetaTotal = celda("TOTAL A PAGAR", FUENTE_ENCABEZADO_TABLA, Element.ALIGN_LEFT);
        etiquetaTotal.setBackgroundColor(AZUL_EPQ);
        PdfPCell valorTotal = celda(FormatosTexto.moneda(factura.getTotal()), FUENTE_ENCABEZADO_TABLA, Element.ALIGN_RIGHT);
        valorTotal.setBackgroundColor(AZUL_EPQ);
        liquidacion.addCell(etiquetaTotal);
        liquidacion.addCell(valorTotal);
        liquidacion.setSpacingAfter(6);
        documento.add(liquidacion);

        documento.add(tablaDatos(new String[][]{
                {"Fecha de emisión", FormatosTexto.fechaHora(factura.getFechaGeneracion())},
                {"Pague antes de", FormatosTexto.fecha(factura.getFechaVencimiento())},
                {"Estado", factura.getEstadoCobro() == null ? "PENDIENTE" : factura.getEstadoCobro().name()}
        }));

        if (!historial.isEmpty()) {
            documento.add(seccion("Consumos de los últimos meses"));
            PdfPTable tabla = new PdfPTable(3);
            tabla.setWidthPercentage(60);
            tabla.setHorizontalAlignment(Element.ALIGN_LEFT);
            tabla.addCell(celdaEncabezado("Período"));
            tabla.addCell(celdaEncabezado("Consumo (m³)"));
            tabla.addCell(celdaEncabezado("Total"));
            for (Factura f : historial) {
                tabla.addCell(celda(f.getPeriodo().toString(), FUENTE_TABLA, Element.ALIGN_CENTER));
                tabla.addCell(celda(f.getConsumo().toPlainString(), FUENTE_TABLA, Element.ALIGN_RIGHT));
                tabla.addCell(celda(FormatosTexto.moneda(f.getTotal()), FUENTE_TABLA, Element.ALIGN_RIGHT));
            }
            documento.add(tabla);
        }

        Paragraph aviso = new Paragraph("Puede pagar en línea desde el portal de autogestión de EPQ o en los puntos de "
                + "atención autorizados. La factura vencida genera gestión de cobro (RN-03).",
                new Font(Font.HELVETICA, 8, Font.ITALIC));
        aviso.setSpacingBefore(10);
        documento.add(aviso);
    }

    private static void agregarFila(PdfPTable tabla, String concepto, String valor) {
        tabla.addCell(celda(concepto, FUENTE_NORMAL, Element.ALIGN_LEFT));
        tabla.addCell(celda(valor, FUENTE_NORMAL, Element.ALIGN_RIGHT));
    }
}
