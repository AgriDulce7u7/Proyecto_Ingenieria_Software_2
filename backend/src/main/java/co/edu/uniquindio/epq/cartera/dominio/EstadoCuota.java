package co.edu.uniquindio.epq.cartera.dominio;

import co.edu.uniquindio.epq.comun.persistencia.CodigoPersistible;

public enum EstadoCuota implements CodigoPersistible {

    PENDIENTE("pendiente"),
    PAGADA("pagada"),
    VENCIDA("vencida");

    private final String codigo;

    EstadoCuota(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String codigo() {
        return codigo;
    }
}
