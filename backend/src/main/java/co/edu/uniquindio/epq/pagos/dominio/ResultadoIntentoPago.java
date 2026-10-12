package co.edu.uniquindio.epq.pagos.dominio;

import co.edu.uniquindio.epq.comun.persistencia.CodigoPersistible;

public enum ResultadoIntentoPago implements CodigoPersistible {

    APROBADO("aprobado"),
    RECHAZADO("rechazado"),
    SIN_RESPUESTA("sin_respuesta");

    private final String codigo;

    ResultadoIntentoPago(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String codigo() {
        return codigo;
    }
}
