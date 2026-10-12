package co.edu.uniquindio.epq.cartera.dominio;

import co.edu.uniquindio.epq.comun.persistencia.CodigoPersistible;

public enum EstadoAcuerdo implements CodigoPersistible {

    VIGENTE("vigente"),
    INCUMPLIDO("incumplido"),
    CUMPLIDO("cumplido");

    private final String codigo;

    EstadoAcuerdo(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String codigo() {
        return codigo;
    }
}
