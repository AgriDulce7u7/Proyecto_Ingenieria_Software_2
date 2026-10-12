package co.edu.uniquindio.epq.seguridad.token;

import co.edu.uniquindio.epq.seguridad.contexto.UsuarioAutenticado;

/**
 * Emisión de tokens de acceso (DIP: el servicio de autenticación no conoce JWT).
 */
public interface ProveedorToken {

    TokenEmitido emitir(UsuarioAutenticado usuario);
}
