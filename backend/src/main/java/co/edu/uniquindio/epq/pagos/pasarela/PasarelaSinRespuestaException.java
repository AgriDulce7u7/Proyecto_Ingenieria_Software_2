package co.edu.uniquindio.epq.pagos.pasarela;

/**
 * CO-02: la pasarela no respondió después de agotar los intentos permitidos.
 *
 * <p>Es una excepción técnica de la capa de integración: no conoce HTTP ni las reglas del caso de uso. El
 * servicio que usa el procesador decide cómo informarla al usuario.</p>
 */
public class PasarelaSinRespuestaException extends RuntimeException {

    private final int intentos;

    public PasarelaSinRespuestaException(int intentos) {
        super("La pasarela no respondió después de %d intentos".formatted(intentos));
        this.intentos = intentos;
    }

    /** Número de intentos realizados antes de desistir. */
    public int intentos() {
        return intentos;
    }
}
