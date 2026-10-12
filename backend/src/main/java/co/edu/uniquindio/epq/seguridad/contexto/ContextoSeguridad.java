package co.edu.uniquindio.epq.seguridad.contexto;

import java.util.Optional;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;

/**
 * Acceso al usuario autenticado de la petición actual (DIP): los servicios no dependen de
 * Spring Security directamente y en las pruebas se reemplaza por un doble.
 */
public interface ContextoSeguridad {

    String USUARIO_SISTEMA = "SISTEMA";

    Optional<UsuarioAutenticado> usuarioActual();

    /** Identificador para auditoría; "SISTEMA" cuando no hay usuario (tareas programadas). */
    default String identificadorActual() {
        return usuarioActual().map(UsuarioAutenticado::identificadorAuditoria).orElse(USUARIO_SISTEMA);
    }

    default UsuarioAutenticado requerirUsuario() {
        return usuarioActual().orElseThrow(() -> new ReglaNegocioException("La operación requiere un usuario autenticado"));
    }
}
