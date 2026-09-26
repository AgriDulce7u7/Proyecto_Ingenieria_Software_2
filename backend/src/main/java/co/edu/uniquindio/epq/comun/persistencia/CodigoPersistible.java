package co.edu.uniquindio.epq.comun.persistencia;

/**
 * Contrato para enums cuyo valor en la base de datos difiere del nombre Java
 * (por ejemplo {@code EN_PROCESO} se guarda como {@code 'en_proceso'}).
 */
public interface CodigoPersistible {

    String codigo();
}
