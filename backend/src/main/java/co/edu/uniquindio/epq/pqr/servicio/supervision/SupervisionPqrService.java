package co.edu.uniquindio.epq.pqr.servicio.supervision;

import java.time.LocalDate;
import java.util.List;

import co.edu.uniquindio.epq.comun.exportacion.ArchivoGenerado;
import co.edu.uniquindio.epq.comun.exportacion.FormatoExportacion;
import co.edu.uniquindio.epq.pqr.web.dto.AlertaPqrResponse;
import co.edu.uniquindio.epq.pqr.web.dto.CumplimientoPqrResponse;

/**
 * StR-02c y SyR-02f: supervisión de la Oficina de PQR.
 */
public interface SupervisionPqrService {

    /** Alertas de vencimiento de las PQR pendientes del equipo del jefe (o de toda la oficina). */
    List<AlertaPqrResponse> alertasDelEquipo(Integer jefeId);

    CumplimientoPqrResponse cumplimiento(LocalDate desde, LocalDate hasta);

    ArchivoGenerado exportarIncumplimientos(LocalDate desde, LocalDate hasta, FormatoExportacion formato);
}
