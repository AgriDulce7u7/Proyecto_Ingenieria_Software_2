package co.edu.uniquindio.epq.comercial.dominio;

import co.edu.uniquindio.epq.comun.persistencia.CodigoPersistible;

/**
 * Resultado de la validación de identidad contra la RNEC (IS-03, SWR-14).
 */
public enum EstadoValidacionIdentidad implements CodigoPersistible {

    VALIDADO("validado"),
    /** CU-01 4b: la RNEC no estaba disponible o no confirmó los datos; se registró con advertencia. */
    PENDIENTE("pendiente"),
    RECHAZADO("rechazado");

    private final String codigo;

    EstadoValidacionIdentidad(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String codigo() {
        return codigo;
    }
}
