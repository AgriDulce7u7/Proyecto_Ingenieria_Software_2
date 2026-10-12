package co.edu.uniquindio.epq.comercial.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MedidorResponse(String numeroSerie, LocalDate fechaInstalacion, String estado, LocalDateTime fechaRetiro) {
}
