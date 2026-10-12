package co.edu.uniquindio.epq.portal.web.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * SWR-26: historial de los últimos 24 meses para graficar y tabular. Los meses sin factura se incluyen
 * con valores nulos para que la gráfica conserve la escala de tiempo.
 */
public record HistorialConsumosResponse(
        String numeroContrato,
        String servicio,
        String unidad,
        String desde,
        String hasta,
        List<PuntoConsumo> meses,
        Resumen resumen) {

    public record PuntoConsumo(String periodo, BigDecimal consumo, BigDecimal valorFacturado, String numeroFactura,
                               String estadoFactura) {
    }

    public record Resumen(int mesesConDatos, BigDecimal consumoPromedio, BigDecimal consumoMaximo,
                          BigDecimal consumoMinimo, BigDecimal totalFacturado) {
    }
}
