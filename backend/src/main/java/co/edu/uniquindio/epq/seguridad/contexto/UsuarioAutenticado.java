package co.edu.uniquindio.epq.seguridad.contexto;

import co.edu.uniquindio.epq.seguridad.Rol;

/**
 * Usuario de la petición actual, reconstruido a partir del token JWT.
 *
 * @param id          id del funcionario o del usuario del servicio (cliente)
 * @param usuario     login del funcionario o número de documento del ciudadano
 * @param gestorId    gestor de PQR vinculado (solo roles de la Oficina de PQR)
 */
public record UsuarioAutenticado(
        Integer id,
        String usuario,
        String nombre,
        Rol rol,
        TipoUsuario tipo,
        Integer gestorId,
        Integer municipioId) {

    public boolean esCiudadano() {
        return tipo == TipoUsuario.CIUDADANO;
    }

    public boolean tieneRol(Rol... roles) {
        for (Rol r : roles) {
            if (r == rol) {
                return true;
            }
        }
        return false;
    }

    /** Identificador que queda en el log de auditoría. */
    public String identificadorAuditoria() {
        return esCiudadano() ? "ciudadano:" + usuario : usuario;
    }
}
