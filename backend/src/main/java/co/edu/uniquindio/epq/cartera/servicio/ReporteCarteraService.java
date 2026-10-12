package co.edu.uniquindio.epq.cartera.servicio;

import co.edu.uniquindio.epq.cartera.web.dto.ReporteCarteraResponse;
import co.edu.uniquindio.epq.comun.exportacion.ArchivoGenerado;
import co.edu.uniquindio.epq.comun.exportacion.FormatoExportacion;

/**
 * RF-11 / SWR-29: reporte de cartera morosa (usuario, deuda y antigüedad), exportable a PDF y Excel.
 */
public interface ReporteCarteraService {

    ReporteCarteraResponse morosos(Integer municipioId, Integer minPeriodos, boolean soloCandidatos);

    ArchivoGenerado exportar(Integer municipioId, Integer minPeriodos, boolean soloCandidatos, FormatoExportacion formato);
}
