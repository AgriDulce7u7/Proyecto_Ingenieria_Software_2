package co.edu.uniquindio.epq.seguridad.servicio;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.seguridad.SeguridadProperties;
import co.edu.uniquindio.epq.seguridad.dominio.CuentaConCredenciales;
import co.edu.uniquindio.epq.seguridad.dominio.PoliticaBloqueo;

/**
 * Verifica credenciales de cualquier {@link CuentaConCredenciales} aplicando la política de bloqueo
 * (CU-05 2a: 3 intentos fallidos bloquean la cuenta 15 minutos).
 */
@Component
public class VerificadorCredenciales {

    /** Hash BCrypt válido usado para igualar tiempos cuando la cuenta no existe (evita enumerar usuarios). */
    private static final String HASH_FICTICIO = "$2a$10$ZQBt5O0qgt2Sk/UbfnOfzOkCyj8rNaTu1TpKF/oqyoQvVq53Uwov.";

    private final PasswordEncoder codificador;
    private final PoliticaBloqueo politica;

    public VerificadorCredenciales(PasswordEncoder codificador, SeguridadProperties propiedades) {
        this.codificador = codificador;
        this.politica = new PoliticaBloqueo(propiedades.maxIntentosFallidos(), propiedades.minutosBloqueo());
    }

    /**
     * @throws CredencialesInvalidasException si la cuenta no existe, está inactiva o la contraseña no coincide
     * @throws CuentaBloqueadaException       si la cuenta está bloqueada o se bloquea con este intento
     */
    public void verificar(CuentaConCredenciales cuenta, String contrasena, LocalDateTime ahora) {
        if (cuenta == null) {
            codificador.matches(contrasena, HASH_FICTICIO);
            throw new CredencialesInvalidasException(null);
        }
        if (cuenta.controlAcceso().estaBloqueado(ahora)) {
            throw new CuentaBloqueadaException(cuenta.controlAcceso().getBloqueadoHasta());
        }
        if (!cuenta.puedeIniciarSesion()) {
            codificador.matches(contrasena, HASH_FICTICIO);
            throw new CredencialesInvalidasException(null);
        }
        if (!codificador.matches(contrasena, cuenta.getPasswordHash())) {
            boolean bloqueada = cuenta.controlAcceso().registrarFallo(ahora, politica);
            if (bloqueada) {
                throw new CuentaBloqueadaException(cuenta.controlAcceso().getBloqueadoHasta());
            }
            throw new CredencialesInvalidasException(cuenta.controlAcceso().intentosRestantes(politica));
        }
        cuenta.registrarAccesoExitoso(ahora);
    }

    public PoliticaBloqueo politica() {
        return politica;
    }
}
