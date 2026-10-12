package co.edu.uniquindio.epq.seguridad.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** RF-07: el ciudadano ingresa con su número de identificación y contraseña. */
public record LoginCiudadanoRequest(
        @NotBlank(message = "El número de identificación es obligatorio") @Size(max = 20) String numeroDocumento,
        @NotBlank(message = "La contraseña es obligatoria") @Size(max = 100) String contrasena) {
}
