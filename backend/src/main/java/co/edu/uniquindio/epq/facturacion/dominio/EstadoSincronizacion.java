package co.edu.uniquindio.epq.facturacion.dominio;

import co.edu.uniquindio.epq.comun.persistencia.CodigoPersistible;

/**
 * Resultado de un intento de sincronización con el ERP financiero (IS-01).
 */
public enum EstadoSincronizacion implements CodigoPersistible {

    EXITOSO("exitoso"),
    FALLIDO("fallido");

    private final String codigo;

    EstadoSincronizacion(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String codigo() {
        return codigo;
    }
}
