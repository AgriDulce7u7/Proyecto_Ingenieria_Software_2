package co.edu.uniquindio.epq.comercial.rnec;

/**
 * Puerto hacia el servicio de consulta de la Registraduría Nacional del Estado Civil (IS-03).
 * Patrón <b>Adapter</b>: hay un adaptador simulado (prototipo) y uno REST; se elige con {@code app.rnec.modo}.
 *
 * <p>Las implementaciones no deben lanzar excepciones por indisponibilidad: retornan
 * {@link ResultadoValidacionRnec.Estado#NO_DISPONIBLE} para que el registro continúe (SWR-14).</p>
 */
public interface ValidadorIdentidadRnec {

    ResultadoValidacionRnec validar(SolicitudValidacionRnec solicitud);
}
