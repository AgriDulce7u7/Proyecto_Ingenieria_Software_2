package co.edu.uniquindio.epq.seguridad.web.dto;

/**
 * Política mínima de contraseñas: 8 a 64 caracteres con mayúscula, minúscula y número.
 */
public final class PoliticaContrasena {

    public static final String EXPRESION = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,64}$";
    public static final String MENSAJE =
            "La contraseña debe tener entre 8 y 64 caracteres e incluir mayúsculas, minúsculas y números";

    private PoliticaContrasena() {
    }
}
