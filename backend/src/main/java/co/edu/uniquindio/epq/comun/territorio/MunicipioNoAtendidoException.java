package co.edu.uniquindio.epq.comun.territorio;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;

/**
 * CO-03: el municipio no pertenece a la lista de municipios atendidos por EPQ.
 */
public class MunicipioNoAtendidoException extends ReglaNegocioException {

    public MunicipioNoAtendidoException(Object municipio) {
        super("El municipio '%s' no es atendido por EPQ. Consulte la lista en /api/municipios".formatted(municipio));
    }
}
