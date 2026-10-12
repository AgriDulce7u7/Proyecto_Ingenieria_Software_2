package co.edu.uniquindio.epq.cartera.web.dto;

import java.time.LocalDateTime;

public record ProcesoCobroResponse(String numeroContrato, String usuario, String servicio, String estado,
                                   LocalDateTime fechaInicio, LocalDateTime fechaActualizacion, String motivo) {
}
