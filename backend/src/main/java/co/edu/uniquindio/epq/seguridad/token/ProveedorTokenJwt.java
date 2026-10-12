package co.edu.uniquindio.epq.seguridad.token;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.seguridad.SeguridadProperties;
import co.edu.uniquindio.epq.seguridad.contexto.ClaimsToken;
import co.edu.uniquindio.epq.seguridad.contexto.UsuarioAutenticado;

/**
 * Emite tokens JWT firmados con HMAC-SHA256. El token incluye el rol (para RBAC) y los datos que el
 * frontend necesita para adaptar el menú (nombre, tipo de usuario, gestor vinculado).
 */
@Component
public class ProveedorTokenJwt implements ProveedorToken {

    private final JwtEncoder codificador;
    private final SeguridadProperties propiedades;
    private final Clock clock;

    public ProveedorTokenJwt(JwtEncoder codificador, SeguridadProperties propiedades, Clock clock) {
        this.codificador = codificador;
        this.propiedades = propiedades;
        this.clock = clock;
    }

    @Override
    public TokenEmitido emitir(UsuarioAutenticado usuario) {
        Instant ahora = clock.instant();
        Instant expira = ahora.plus(Duration.ofMinutes(propiedades.jwt().expiracionMinutos()));
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(propiedades.jwt().emisor())
                .issuedAt(ahora)
                .expiresAt(expira)
                .subject(usuario.usuario())
                .claim(ClaimsToken.ID, usuario.id())
                .claim(ClaimsToken.TIPO, usuario.tipo().name())
                .claim(ClaimsToken.ROL, usuario.rol().name())
                .claim(ClaimsToken.ROLES, List.of(usuario.rol().name()))
                .claim(ClaimsToken.NOMBRE, usuario.nombre());
        // Los claims nulos no se permiten en JWT: solo se agregan si existen
        if (usuario.gestorId() != null) {
            claims.claim(ClaimsToken.GESTOR_ID, usuario.gestorId());
        }
        if (usuario.municipioId() != null) {
            claims.claim(ClaimsToken.MUNICIPIO_ID, usuario.municipioId());
        }
        JwsHeader encabezado = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = codificador.encode(JwtEncoderParameters.from(encabezado, claims.build())).getTokenValue();
        return new TokenEmitido(token, expira);
    }
}
