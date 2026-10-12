package co.edu.uniquindio.epq.comercial.rnec;

import co.edu.uniquindio.epq.comercial.dominio.EstadoValidacionIdentidad;

/**
 * Respuesta de la RNEC (IS-03).
 */
public record ResultadoValidacionRnec(Estado estado, String mensaje) {

    public enum Estado {
        VALIDADA,
        NO_COINCIDE,
        NO_ENCONTRADA,
        NO_DISPONIBLE
    }

    public static ResultadoValidacionRnec noDisponible(String mensaje) {
        return new ResultadoValidacionRnec(Estado.NO_DISPONIBLE, mensaje);
    }

    /** CU-01 4b: cualquier resultado distinto de VALIDADA deja al usuario "pendiente de validación". */
    public EstadoValidacionIdentidad estadoUsuario() {
        return estado == Estado.VALIDADA ? EstadoValidacionIdentidad.VALIDADO : EstadoValidacionIdentidad.PENDIENTE;
    }

    public boolean esExitosa() {
        return estado == Estado.VALIDADA;
    }
}
