package co.edu.uniquindio.epq.cartera.servicio;

import co.edu.uniquindio.epq.cartera.web.dto.ResultadoMonitoreoCarteraResponse;

/**
 * Monitoreo diario de cartera: vencimiento de facturas, incumplimiento de acuerdos (SWR-31),
 * procesos de cobro y candidatos a suspensión (SWR-32).
 */
public interface MonitoreoCarteraService {

    ResultadoMonitoreoCarteraResponse ejecutar();
}
