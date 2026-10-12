package co.edu.uniquindio.epq.seguridad.servicio;

import co.edu.uniquindio.epq.seguridad.web.dto.ActivarCuentaCiudadanoRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.LoginCiudadanoRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.LoginRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.SesionResponse;

/**
 * IS-SIS-01 Módulo de autenticación de funcionarios y ciudadanos.
 */
public interface ServicioAutenticacion {

    SesionResponse iniciarSesionFuncionario(LoginRequest solicitud);

    SesionResponse iniciarSesionCiudadano(LoginCiudadanoRequest solicitud);

    void activarCuentaCiudadano(ActivarCuentaCiudadanoRequest solicitud);

    /** El token JWT no guarda estado en el servidor: el cierre de sesión se audita y el cliente descarta el token. */
    void cerrarSesion();

    SesionResponse.PerfilResponse perfilActual();
}
