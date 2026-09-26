package co.edu.uniquindio.epq.pqr.dominio;

import co.edu.uniquindio.epq.comun.persistencia.CodigoPersistible;

/**
 * Tipos de notificación registrados en la columna {@code notificacion.tipo}.
 */
public enum TipoNotificacion implements CodigoPersistible {

    RADICACION_CIUDADANO("radicacion_ciudadano"),
    ASIGNACION_GESTOR("asignacion_gestor"),
    RESPUESTA_CIUDADANO("respuesta_ciudadano"),
    VENCIMIENTO_48H("vencimiento_48h"),
    PQR_VENCIDA("pqr_vencida");

    private final String codigo;

    TipoNotificacion(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String codigo() {
        return codigo;
    }
}
