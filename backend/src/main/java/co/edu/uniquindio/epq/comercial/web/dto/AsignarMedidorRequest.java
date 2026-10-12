package co.edu.uniquindio.epq.comercial.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Instala (o reemplaza) el medidor del contrato. Si {@code activarContrato} es verdadero y el contrato
 * está pendiente, se activa en la misma operación.
 */
public record AsignarMedidorRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{4,50}", message = "El número de medidor debe tener entre 4 y 50 caracteres")
        String numeroSerie,
        LocalDate fechaInstalacion,
        Boolean activarContrato) {
}
