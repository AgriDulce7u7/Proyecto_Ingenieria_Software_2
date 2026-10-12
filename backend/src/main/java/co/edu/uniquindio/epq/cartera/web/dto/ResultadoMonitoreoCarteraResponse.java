package co.edu.uniquindio.epq.cartera.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ResultadoMonitoreoCarteraResponse(LocalDateTime fechaEjecucion, List<Paso> pasos) {

    public record Paso(String nombre, int registrosAfectados, String detalle) {
    }
}
