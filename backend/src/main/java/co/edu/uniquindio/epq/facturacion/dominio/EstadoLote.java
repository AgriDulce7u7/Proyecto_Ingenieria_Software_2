package co.edu.uniquindio.epq.facturacion.dominio;

import co.edu.uniquindio.epq.comun.persistencia.CodigoPersistible;

/**
 * Estados de un lote mensual de facturación.
 */
public enum EstadoLote implements CodigoPersistible {

    EN_PROCESO("en_proceso"),
    COMPLETADO("completado"),
    ERROR("error");

    private final String codigo;

    EstadoLote(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String codigo() {
        return codigo;
    }
}
