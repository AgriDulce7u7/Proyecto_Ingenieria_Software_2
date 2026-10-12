package co.edu.uniquindio.epq.seguridad.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RestablecerContrasenaRequest(
        @NotBlank @Pattern(regexp = PoliticaContrasena.EXPRESION, message = PoliticaContrasena.MENSAJE)
        String nuevaContrasena) {
}
