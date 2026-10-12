package co.edu.uniquindio.epq.seguridad.web.dto;

import co.edu.uniquindio.epq.seguridad.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** RF-15: los cambios de rol quedan auditados con su motivo. */
public record CambiarRolRequest(
        @NotNull(message = "El rol es obligatorio") Rol rol,
        Integer gestorId,
        @NotBlank(message = "El motivo del cambio es obligatorio") @Size(max = 300) String motivo) {
}
