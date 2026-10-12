package co.edu.uniquindio.epq.comercial.web.dto;

import co.edu.uniquindio.epq.comercial.dominio.EstadoContrato;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** SWR-20: el cambio de estado registra motivo, fecha y responsable. */
public record CambiarEstadoContratoRequest(
        @NotNull(message = "El nuevo estado es obligatorio (ACTIVO, SUSPENDIDO o CANCELADO)") EstadoContrato estado,
        @NotBlank(message = "El motivo es obligatorio") @Size(max = 500) String motivo) {
}
