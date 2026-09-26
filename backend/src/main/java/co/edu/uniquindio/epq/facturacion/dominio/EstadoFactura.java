package co.edu.uniquindio.epq.facturacion.dominio;

import co.edu.uniquindio.epq.comun.persistencia.CodigoPersistible;

/**
 * Estados de una factura según la columna {@code factura.estado}.
 */
public enum EstadoFactura implements CodigoPersistible {

    GENERADA("generada"),
    SINCRONIZADA("sincronizada"),
    ERROR_SINCRONIZACION("error_sincronizacion"),
    PAGADA("pagada");

    private final String codigo;

    EstadoFactura(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String codigo() {
        return codigo;
    }
}
