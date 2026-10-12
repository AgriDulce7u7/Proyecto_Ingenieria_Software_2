package co.edu.uniquindio.epq.comercial.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** RF-02: actualización de datos. La identificación no se modifica (SWR-12). */
public record ActualizarUsuarioRequest(
        @NotBlank @Size(max = 150) String nombreCompleto,
        @NotBlank @Pattern(regexp = "[0-9+ ]{7,20}", message = "El teléfono debe tener entre 7 y 20 dígitos") String telefono,
        @NotBlank @Email @Size(max = 150) String correo,
        @NotBlank @Size(max = 200) String direccionNotificacion,
        @NotNull(message = "El municipio es obligatorio") Integer municipioId) {
}
