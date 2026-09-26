package co.edu.uniquindio.epq.pqr.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CerrarPqrRequest(
        @NotBlank(message = "El usuario que cierra la PQR es obligatorio") @Size(max = 150) String usuario,
        @Size(max = 500) String observacion) {
}
