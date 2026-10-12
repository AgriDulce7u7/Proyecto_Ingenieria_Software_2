package co.edu.uniquindio.epq.pagos.pasarela;

/**
 * Puerto hacia la pasarela de pagos electrónicos (IS-02). Patrón <b>Adapter</b>.
 *
 * <p>Un rechazo de negocio se retorna como {@link RespuestaPasarela} no aprobada; la falta de respuesta
 * se señala con {@link PasarelaNoDisponibleException}.</p>
 */
public interface PasarelaPagosGateway {

    RespuestaPasarela cobrar(SolicitudCobro solicitud);
}
