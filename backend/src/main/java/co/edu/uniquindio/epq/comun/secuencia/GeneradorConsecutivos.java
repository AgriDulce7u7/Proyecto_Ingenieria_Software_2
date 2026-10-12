package co.edu.uniquindio.epq.comun.secuencia;

/**
 * Abstracción para obtener consecutivos únicos (DIP): permite reemplazar las secuencias de la base
 * de datos por otro mecanismo y simular los consecutivos en las pruebas unitarias.
 */
public interface GeneradorConsecutivos {

    long siguiente(SecuenciaNegocio secuencia);
}
