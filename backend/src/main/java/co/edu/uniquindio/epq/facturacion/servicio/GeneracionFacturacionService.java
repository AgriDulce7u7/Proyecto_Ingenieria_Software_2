package co.edu.uniquindio.epq.facturacion.servicio;

import java.time.YearMonth;

import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoGeneracionLoteResponse;

/**
 * Generación masiva de facturas mensuales (RF-05, CU-03, SWR-01..04). Patrón <b>Facade</b>.
 */
public interface GeneracionFacturacionService {

    String ORIGEN_AUTOMATICO = "AUTOMATICO";
    String ORIGEN_MANUAL = "MANUAL";

    /**
     * Genera (o completa) el lote del periodo de consumo indicado y sincroniza sus facturas con el ERP.
     *
     * @param periodo periodo de consumo; si es {@code null} se usa el mes anterior al actual
     * @param origen  quién disparó la ejecución (automático o manual)
     */
    ResultadoGeneracionLoteResponse generarLote(YearMonth periodo, String origen);

    /** Periodo que corresponde facturar hoy: el mes de consumo anterior. */
    YearMonth periodoPorDefecto();
}
