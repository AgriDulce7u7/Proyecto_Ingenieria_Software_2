package co.edu.uniquindio.epq.seguridad.servicio;

import java.util.Optional;

/**
 * Puerto para obtener la cuenta de portal de un usuario del servicio (DIP): el módulo de seguridad no
 * depende del módulo comercial; este provee la implementación.
 */
public interface RepositorioCuentasCiudadano {

    Optional<CuentaCiudadano> buscarPorNumeroDocumento(String numeroDocumento);
}
