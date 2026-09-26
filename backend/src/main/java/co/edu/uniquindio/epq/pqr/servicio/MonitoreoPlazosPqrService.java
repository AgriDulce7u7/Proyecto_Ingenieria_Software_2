package co.edu.uniquindio.epq.pqr.servicio;

import co.edu.uniquindio.epq.pqr.web.dto.ResultadoMonitoreoResponse;

/**
 * Vigilancia de plazos normativos: alerta de 48 h (SWR-07) y vencimiento (RN-04, CU-06 9a).
 */
public interface MonitoreoPlazosPqrService {

    ResultadoMonitoreoResponse ejecutarMonitoreo();
}
