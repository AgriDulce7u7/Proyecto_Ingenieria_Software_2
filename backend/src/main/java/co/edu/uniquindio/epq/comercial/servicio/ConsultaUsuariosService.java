package co.edu.uniquindio.epq.comercial.servicio;

import java.util.List;

import co.edu.uniquindio.epq.comercial.web.dto.PredioResponse;
import co.edu.uniquindio.epq.comercial.web.dto.UsuarioDetalleResponse;
import co.edu.uniquindio.epq.comercial.web.dto.UsuarioResumenResponse;

/**
 * RF-03: consulta de usuarios por identificación, nombre o dirección del predio (SWR-16: menos de 3 s).
 */
public interface ConsultaUsuariosService {

    List<UsuarioResumenResponse> buscar(String identificacion, String nombre, String direccion);

    UsuarioDetalleResponse obtener(String codigoInterno);

    List<PredioResponse> predios(String codigoInterno);
}
