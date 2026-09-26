package co.edu.uniquindio.epq.pqr.servicio.plazo;

import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitudTipo;

/**
 * Patrón <b>Strategy</b>: política de plazo normativo por tipo de solicitud (RN-04).
 * Para agregar un nuevo tipo (p. ej. recursos de reposición) basta con crear una nueva
 * implementación anotada con {@code @Component}, sin modificar el calculador (OCP).
 */
public interface PoliticaPlazoRespuesta {

    boolean aplicaA(TipoSolicitudTipo tipo);

    int diasHabiles();
}
