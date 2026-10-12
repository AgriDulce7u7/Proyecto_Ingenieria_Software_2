package co.edu.uniquindio.epq.portal.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Tabla 7: el ciudadano puede actualizar sus datos de contacto desde el portal. */
public record ActualizarContactoRequest(
        @NotBlank @Email @Size(max = 150) String correo,
        @NotBlank @Pattern(regexp = "[0-9+ ]{7,20}", message = "El teléfono debe tener entre 7 y 20 dígitos") String telefono,
        @NotBlank @Size(max = 200) String direccionNotificacion) {
}
