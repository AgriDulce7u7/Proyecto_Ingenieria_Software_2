package co.edu.uniquindio.epq.pqr.web.dto;

import java.time.LocalDateTime;

public record ResultadoMonitoreoResponse(LocalDateTime fechaEjecucion, int alertasEnviadas, int pqrMarcadasVencidas) {
}
