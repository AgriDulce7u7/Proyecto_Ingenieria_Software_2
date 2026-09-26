package co.edu.uniquindio.epq.facturacion.servicio.generacion;

/**
 * Resultado posible al intentar facturar un contrato dentro de un lote.
 */
public enum TipoResultadoFacturacion {

    GENERADA("Factura generada"),
    YA_FACTURADO("El contrato ya tenía factura para el periodo"),
    NO_ACTIVO("El contrato no está activo"),
    SIN_MEDIDOR("El contrato no tiene un medidor activo"),
    SIN_LECTURA("No hay lectura registrada para el periodo"),
    SIN_TARIFA("No hay tarifa vigente para el servicio"),
    ERROR("Error inesperado al liquidar");

    private final String descripcion;

    TipoResultadoFacturacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** Incidencias que requieren atención del área comercial (no se generó factura). */
    public boolean esIncidencia() {
        return this != GENERADA && this != YA_FACTURADO;
    }
}
