package co.edu.uniquindio.epq.comercial.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MotivoRequest(@NotBlank(message = "El motivo es obligatorio") @Size(max = 500) String motivo) {
}
