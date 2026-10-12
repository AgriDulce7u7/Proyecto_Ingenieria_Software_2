package co.edu.uniquindio.epq.seguridad.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambiarEstadoFuncionarioRequest(
        @NotNull Boolean activo,
        @NotBlank(message = "El motivo es obligatorio") @Size(max = 300) String motivo) {
}
