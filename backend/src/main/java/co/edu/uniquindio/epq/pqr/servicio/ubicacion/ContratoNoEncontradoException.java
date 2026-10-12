package co.edu.uniquindio.epq.pqr.servicio.ubicacion;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;

/**
 * CO-01 / CU-06 4a: el número de contrato no existe o no está activo.
 */
public class ContratoNoEncontradoException extends ReglaNegocioException {

    public ContratoNoEncontradoException(String numeroContrato) {
        super(("El contrato %s no existe o no está activo. Verifique el número o acuda a una oficina de atención "
                + "de EPQ.").formatted(numeroContrato));
    }
}
