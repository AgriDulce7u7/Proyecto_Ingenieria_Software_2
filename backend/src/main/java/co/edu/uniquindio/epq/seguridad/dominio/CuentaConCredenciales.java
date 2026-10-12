package co.edu.uniquindio.epq.seguridad.dominio;

import java.time.LocalDateTime;

/**
 * Contrato común de las cuentas que inician sesión (funcionarios y ciudadanos). Gracias a esta
 * abstracción el verificador de credenciales trata ambos tipos de cuenta por igual (LSP / DIP).
 */
public interface CuentaConCredenciales {

    String getPasswordHash();

    /** La cuenta está activa y tiene contraseña definida. */
    boolean puedeIniciarSesion();

    ControlAcceso controlAcceso();

    /** Gancho para registrar la fecha del último acceso (opcional). */
    default void registrarAccesoExitoso(LocalDateTime ahora) {
        controlAcceso().reiniciar();
    }
}
