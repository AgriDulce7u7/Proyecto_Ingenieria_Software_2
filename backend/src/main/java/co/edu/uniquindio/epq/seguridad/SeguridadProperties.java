package co.edu.uniquindio.epq.seguridad;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parámetros de seguridad.
 *
 * @param jwt                 configuración del token
 * @param maxIntentosFallidos intentos fallidos antes de bloquear la cuenta (CU-05 2a: 3)
 * @param minutosBloqueo      duración del bloqueo temporal (CU-05 2a: 15 minutos)
 */
@ConfigurationProperties(prefix = "app.seguridad")
public record SeguridadProperties(
        @DefaultValue Jwt jwt,
        @DefaultValue("3") int maxIntentosFallidos,
        @DefaultValue("15") int minutosBloqueo) {

    /**
     * @param secreto           clave HMAC-SHA256 (mínimo 32 caracteres). En producción se define con la variable JWT_SECRET.
     * @param expiracionMinutos vigencia del token
     * @param emisor            valor del claim {@code iss}
     */
    public record Jwt(
            @DefaultValue("sigca-epq-clave-de-desarrollo-cambiar-en-produccion-2026") String secreto,
            @DefaultValue("480") long expiracionMinutos,
            @DefaultValue("sigca-epq") String emisor) {

        public Jwt {
            if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
                throw new IllegalStateException("app.seguridad.jwt.secreto (JWT_SECRET) debe tener al menos 32 bytes");
            }
        }
    }
}
