package co.edu.uniquindio.epq.pagos.dominio;

import co.edu.uniquindio.epq.comun.persistencia.CodigoPersistible;

/** SWR-24: canal por el que se realizó el pago. */
public enum CanalPago implements CodigoPersistible {

    PRESENCIAL("presencial"),
    EN_LINEA("en_linea"),
    BANCO("banco");

    private final String codigo;

    CanalPago(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String codigo() {
        return codigo;
    }
}
