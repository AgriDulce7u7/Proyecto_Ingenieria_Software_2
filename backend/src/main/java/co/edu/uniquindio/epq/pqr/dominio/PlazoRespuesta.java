package co.edu.uniquindio.epq.pqr.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Objeto valor con el plazo normativo de respuesta de una PQR (RN-04 / SWR-06).
 *
 * @param fechaLimite   instante exacto en que vence el plazo (usado por la alerta de 48 h)
 * @param fechaEstimada fecha mostrada al ciudadano
 */
public record PlazoRespuesta(LocalDateTime fechaLimite, LocalDate fechaEstimada) {

    public PlazoRespuesta {
        Objects.requireNonNull(fechaLimite, "La fecha límite es obligatoria");
        Objects.requireNonNull(fechaEstimada, "La fecha estimada es obligatoria");
    }
}
