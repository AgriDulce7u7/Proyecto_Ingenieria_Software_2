package co.edu.uniquindio.epq.seguridad.web.dto;

import co.edu.uniquindio.epq.seguridad.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * CU-10: crear la cuenta de un funcionario. {@code gestorId} es obligatorio para GESTOR_PQR y JEFE_PQR.
 */
public record CrearFuncionarioRequest(
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9._-]{3,60}", message = "El usuario debe tener 3 a 60 caracteres (letras, números, punto, guion)")
        String usuario,
        @NotBlank @Size(max = 150) String nombreCompleto,
        @NotBlank @Email @Size(max = 150) String correo,
        @NotNull(message = "El rol es obligatorio") Rol rol,
        Integer municipioId,
        Integer gestorId,
        @NotBlank @Pattern(regexp = PoliticaContrasena.EXPRESION, message = PoliticaContrasena.MENSAJE)
        String contrasenaTemporal) {
}
