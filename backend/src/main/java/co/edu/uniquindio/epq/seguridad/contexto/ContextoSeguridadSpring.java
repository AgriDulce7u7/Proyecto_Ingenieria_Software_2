package co.edu.uniquindio.epq.seguridad.contexto;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.seguridad.Rol;

/**
 * Implementación basada en el {@code SecurityContext} de Spring Security y el token JWT.
 */
@Component
public class ContextoSeguridadSpring implements ContextoSeguridad {

    @Override
    public Optional<UsuarioAutenticado> usuarioActual() {
        SecurityContext contexto = SecurityContextHolder.getContext();
        Authentication autenticacion = contexto == null ? null : contexto.getAuthentication();
        if (autenticacion instanceof JwtAuthenticationToken tokenJwt) {
            return Optional.of(desdeJwt(tokenJwt.getToken()));
        }
        return Optional.empty();
    }

    static UsuarioAutenticado desdeJwt(Jwt jwt) {
        return new UsuarioAutenticado(
                entero(jwt.getClaim(ClaimsToken.ID)),
                jwt.getSubject(),
                jwt.getClaimAsString(ClaimsToken.NOMBRE),
                Rol.valueOf(jwt.getClaimAsString(ClaimsToken.ROL)),
                TipoUsuario.valueOf(jwt.getClaimAsString(ClaimsToken.TIPO)),
                entero(jwt.getClaim(ClaimsToken.GESTOR_ID)),
                entero(jwt.getClaim(ClaimsToken.MUNICIPIO_ID)));
    }

    /** Nimbus deserializa los números del token como Long. */
    private static Integer entero(Object valor) {
        return valor instanceof Number numero ? numero.intValue() : null;
    }
}
