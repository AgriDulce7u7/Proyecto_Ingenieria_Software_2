package co.edu.uniquindio.epq.pqr.servicio.plazo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.comun.calendario.CalendarioLaboral;
import co.edu.uniquindio.epq.pqr.dominio.PlazoRespuesta;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitudTipo;

/**
 * Calcula la fecha límite de respuesta de una PQR en días hábiles (SWR-06), excluyendo fines de
 * semana y festivos nacionales. El plazo vence al final del último día hábil.
 */
@Component
public class CalculadoraPlazoRespuesta {

    private static final LocalTime FIN_DEL_DIA = LocalTime.of(23, 59, 59);

    private final List<PoliticaPlazoRespuesta> politicas;
    private final CalendarioLaboral calendario;

    public CalculadoraPlazoRespuesta(List<PoliticaPlazoRespuesta> politicas, CalendarioLaboral calendario) {
        this.politicas = politicas;
        this.calendario = calendario;
    }

    public PlazoRespuesta calcular(TipoSolicitudTipo tipo, LocalDateTime fechaRecepcion) {
        PoliticaPlazoRespuesta politica = politicas.stream()
                .filter(p -> p.aplicaA(tipo))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay política de plazo para el tipo " + tipo));
        LocalDate fechaLimite = calendario.sumarDiasHabiles(fechaRecepcion.toLocalDate(), politica.diasHabiles());
        return new PlazoRespuesta(fechaLimite.atTime(FIN_DEL_DIA), fechaLimite);
    }
}
