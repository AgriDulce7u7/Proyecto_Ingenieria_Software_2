package co.edu.uniquindio.epq.seguridad.dominio;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Valor embebido con el estado de bloqueo por intentos fallidos. Lo reutilizan las cuentas de
 * funcionarios y de ciudadanos (DRY): la regla de bloqueo vive en un solo lugar.
 */
@Embeddable
public class ControlAcceso {

    @Column(name = "intentos_fallidos", nullable = false)
    private short intentosFallidos;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    public boolean estaBloqueado(LocalDateTime ahora) {
        return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }

    /**
     * Registra un intento fallido. Si se alcanza el máximo, bloquea la cuenta y reinicia el contador.
     *
     * @return {@code true} si la cuenta quedó bloqueada con este intento
     */
    public boolean registrarFallo(LocalDateTime ahora, PoliticaBloqueo politica) {
        if (bloqueadoHasta != null && !ahora.isBefore(bloqueadoHasta)) {
            bloqueadoHasta = null; // el bloqueo anterior ya expiró
        }
        intentosFallidos++;
        if (intentosFallidos >= politica.maxIntentos()) {
            bloqueadoHasta = ahora.plusMinutes(politica.minutosBloqueo());
            intentosFallidos = 0;
            return true;
        }
        return false;
    }

    public void reiniciar() {
        intentosFallidos = 0;
        bloqueadoHasta = null;
    }

    public int intentosRestantes(PoliticaBloqueo politica) {
        return Math.max(0, politica.maxIntentos() - intentosFallidos);
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }
}
