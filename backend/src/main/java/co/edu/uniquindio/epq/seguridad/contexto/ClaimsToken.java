package co.edu.uniquindio.epq.seguridad.contexto;

/**
 * Nombres de los claims del token JWT (contrato con el frontend).
 */
public final class ClaimsToken {

    public static final String ID = "uid";
    public static final String TIPO = "tipo";
    public static final String ROL = "rol";
    public static final String ROLES = "roles";
    public static final String NOMBRE = "nombre";
    public static final String GESTOR_ID = "gestorId";
    public static final String MUNICIPIO_ID = "municipioId";

    private ClaimsToken() {
    }
}
