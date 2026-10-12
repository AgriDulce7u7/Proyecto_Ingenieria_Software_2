package co.edu.uniquindio.epq.pagos.pasarela;

/**
 * Datos del medio de pago. Solo viajan hacia la pasarela: nunca se guardan ni se escriben en el log.
 */
public record DatosTarjeta(String numero, String titular, int mesVencimiento, int anioVencimiento, String cvv) {

    public String ultimosDigitos() {
        return numero == null || numero.length() < 4 ? null : numero.substring(numero.length() - 4);
    }

    /** Evita exponer datos sensibles si el objeto llega a imprimirse. */
    @Override
    public String toString() {
        return "DatosTarjeta[**** **** **** " + ultimosDigitos() + "]";
    }
}
