package co.edu.uniquindio.epq.pqr.web.dto;

/**
 * Gestor de la Oficina de PQR.
 *
 * @param municipio municipio que atiende (asignación por territorio); {@code null} si atiende a toda la oficina.
 * @param esJefe    {@code true} si es el jefe de la oficina (recibe alertas del equipo, SWR-11).
 */
public record GestorResponse(Integer id, String nombre, String correo, String area, boolean activo,
                             String municipio, boolean esJefe) {

    /** Compatibilidad con la primera entrega. */
    public GestorResponse(Integer id, String nombre, String correo, String area, boolean activo) {
        this(id, nombre, correo, area, activo, null, false);
    }
}
