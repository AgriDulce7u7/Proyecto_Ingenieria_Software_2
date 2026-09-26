package co.edu.uniquindio.epq.pqr.web.dto;

import jakarta.validation.constraints.NotNull;

/** El gestor toma la PQR para atención (CU-06 paso 6). */
public record TomarPqrRequest(@NotNull(message = "El gestor es obligatorio") Integer gestorId) {
}
