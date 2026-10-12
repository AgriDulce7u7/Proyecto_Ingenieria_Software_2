package co.edu.uniquindio.epq.pagos.pasarela;

/**
 * Falla técnica al comunicarse con la pasarela (sin respuesta). Se reintenta según CO-02.
 */
public class PasarelaNoDisponibleException extends RuntimeException {

    public PasarelaNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public PasarelaNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
