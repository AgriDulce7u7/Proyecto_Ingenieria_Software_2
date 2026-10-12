package co.edu.uniquindio.epq.seguridad.dominio;

/**
 * Regla de bloqueo temporal por intentos fallidos (CU-05 2a: 3 intentos, 15 minutos).
 */
public record PoliticaBloqueo(int maxIntentos, int minutosBloqueo) {

    public PoliticaBloqueo {
        if (maxIntentos < 1 || minutosBloqueo < 1) {
            throw new IllegalArgumentException("La política de bloqueo requiere valores positivos");
        }
    }
}
