package co.edu.uniquindio.epq.pqr.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Reasignación de la PQR a otro gestor (DE-02: reasignar). */
public record ReasignarPqrRequest(
        @NotNull(message = "El nuevo gestor es obligatorio") Integer gestorId,
        @NotBlank(message = "El usuario que reasigna es obligatorio") @Size(max = 150) String usuario,
        @Size(max = 500) String motivo) {
}
