package co.edu.uniquindio.epq.pqr.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Respuesta formal del gestor (CU-06 paso 9, RN-06). */
public record ResponderPqrRequest(
        @NotNull(message = "El gestor es obligatorio") Integer gestorId,
        @NotBlank(message = "La respuesta formal es obligatoria (RN-06)")
        @Size(min = 20, max = 10000, message = "La respuesta debe tener entre 20 y 10000 caracteres")
        String respuesta) {
}
