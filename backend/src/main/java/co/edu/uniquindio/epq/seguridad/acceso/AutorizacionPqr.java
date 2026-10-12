package co.edu.uniquindio.epq.seguridad.acceso;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.seguridad.Rol;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;

/**
 * Reglas de autorización sobre PQR usadas desde {@code @PreAuthorize}:
 * un Gestor de PQR solo puede actuar en nombre de su propio registro de gestor;
 * el Jefe de la Oficina de PQR y el Administrador pueden actuar sobre cualquiera.
 *
 * <pre>@PreAuthorize("@autorizacionPqr.puedeActuarComo(#solicitud.gestorId())")</pre>
 */
@Component("autorizacionPqr")
public class AutorizacionPqr {

    private final ContextoSeguridad contexto;

    public AutorizacionPqr(ContextoSeguridad contexto) {
        this.contexto = contexto;
    }

    public boolean puedeActuarComo(Integer gestorId) {
        return contexto.usuarioActual()
                .map(u -> u.tieneRol(Rol.JEFE_PQR, Rol.ADMINISTRADOR)
                        || (u.rol() == Rol.GESTOR_PQR && gestorId != null && gestorId.equals(u.gestorId())))
                .orElse(false);
    }
}
