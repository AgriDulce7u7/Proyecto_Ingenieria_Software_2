package co.edu.uniquindio.epq.cartera.dominio;

import co.edu.uniquindio.epq.comun.persistencia.CodigoPersistible;

/** RN-07: el cobro coercitivo se suspende con un acuerdo vigente y se reactiva si se incumple. */
public enum EstadoProcesoCobro implements CodigoPersistible {

    ACTIVO("activo"),
    SUSPENDIDO("suspendido"),
    CERRADO("cerrado");

    private final String codigo;

    EstadoProcesoCobro(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String codigo() {
        return codigo;
    }
}
