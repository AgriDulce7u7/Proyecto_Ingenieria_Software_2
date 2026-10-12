package co.edu.uniquindio.epq.seguridad.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Inicio de sesión de un funcionario. */
public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio") @Size(max = 60) String usuario,
        @NotBlank(message = "La contraseña es obligatoria") @Size(max = 100) String contrasena) {
}
