package co.edu.uniquindio.epq.facturacion.dominio;

import co.edu.uniquindio.epq.comun.persistencia.CodigoPersistible;

/**
 * Estados de un contrato de servicio (EST-06, EST-07). Solo los contratos ACTIVOS se facturan.
 */
public enum EstadoContrato implements CodigoPersistible {

    ACTIVO("activo"),
    SUSPENDIDO("suspendido"),
    INACTIVO("inactivo");

    private final String codigo;

    EstadoContrato(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String codigo() {
        return codigo;
    }
}
