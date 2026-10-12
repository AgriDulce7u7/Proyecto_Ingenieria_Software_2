package co.edu.uniquindio.epq.seguridad.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Autorregistro en el portal (CU-01): el usuario del servicio ya registrado por EPQ define su
 * contraseña verificando su número de identificación y el correo registrado.
 */
public record ActivarCuentaCiudadanoRequest(
        @NotBlank(message = "El número de identificación es obligatorio") @Size(max = 20) String numeroDocumento,
        @NotBlank(message = "El correo registrado es obligatorio") @Email String correo,
        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(regexp = PoliticaContrasena.EXPRESION, message = PoliticaContrasena.MENSAJE)
        String contrasena) {
}
