package co.edu.uniquindio.epq.comun.secuencia;

/**
 * Secuencias de PostgreSQL que generan los códigos de negocio. El enum actúa como lista blanca:
 * nunca se concatena en SQL un nombre de secuencia recibido desde fuera.
 */
public enum SecuenciaNegocio {

    USUARIO("seq_codigo_usuario"),
    PREDIO("seq_codigo_predio"),
    CONTRATO("seq_numero_contrato"),
    ACUERDO_PAGO("seq_numero_acuerdo"),
    PAGO("seq_codigo_pago"),
    COMPROBANTE("seq_numero_comprobante");

    private final String nombreSecuencia;

    SecuenciaNegocio(String nombreSecuencia) {
        this.nombreSecuencia = nombreSecuencia;
    }

    public String nombreSecuencia() {
        return nombreSecuencia;
    }
}
