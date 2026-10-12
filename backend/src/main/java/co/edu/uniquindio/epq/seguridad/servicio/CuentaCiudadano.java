package co.edu.uniquindio.epq.seguridad.servicio;

import co.edu.uniquindio.epq.seguridad.dominio.CuentaConCredenciales;

/**
 * Vista de la cuenta del portal de autogestión de un usuario del servicio (implementada por el
 * módulo comercial).
 */
public interface CuentaCiudadano extends CuentaConCredenciales {

    Integer getId();

    String getNumeroDocumento();

    String getNombreCompleto();

    String getCorreo();

    boolean tieneCuentaPortal();

    boolean isActivo();

    Integer municipioId();

    void definirContrasenaPortal(String hash);
}
