package co.edu.uniquindio.epq.comercial.servicio;

import co.edu.uniquindio.epq.comercial.web.dto.ActualizarUsuarioRequest;
import co.edu.uniquindio.epq.comercial.web.dto.PredioResponse;
import co.edu.uniquindio.epq.comercial.web.dto.RegistrarPredioRequest;
import co.edu.uniquindio.epq.comercial.web.dto.RegistrarUsuarioRequest;
import co.edu.uniquindio.epq.comercial.web.dto.UsuarioDetalleResponse;
import co.edu.uniquindio.epq.comercial.web.dto.UsuarioRegistradoResponse;

/**
 * F-03 / CU-01: comandos sobre usuarios del servicio (Asesor de Comercialización o Administrador).
 */
public interface GestionUsuariosService {

    UsuarioRegistradoResponse registrar(RegistrarUsuarioRequest solicitud);

    UsuarioDetalleResponse actualizar(String codigoInterno, ActualizarUsuarioRequest solicitud);

    UsuarioDetalleResponse desactivar(String codigoInterno, String motivo);

    UsuarioDetalleResponse reactivar(String codigoInterno, String motivo);

    /** Reintenta la validación contra la RNEC de un usuario pendiente de validación. */
    UsuarioDetalleResponse revalidarIdentidad(String codigoInterno);

    PredioResponse registrarPredio(String codigoInterno, RegistrarPredioRequest solicitud);
}
