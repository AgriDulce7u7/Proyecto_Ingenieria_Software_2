package co.edu.uniquindio.epq.pqr.servicio.radicado;

import java.time.LocalDateTime;

/**
 * Genera el número de radicado único de una PQR (SWR-05).
 */
public interface GeneradorRadicado {

    String generar(LocalDateTime fechaRecepcion);
}
