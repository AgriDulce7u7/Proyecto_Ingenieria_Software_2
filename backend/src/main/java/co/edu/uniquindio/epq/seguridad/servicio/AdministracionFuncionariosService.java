package co.edu.uniquindio.epq.seguridad.servicio;

import java.util.List;

import co.edu.uniquindio.epq.seguridad.Rol;
import co.edu.uniquindio.epq.seguridad.web.dto.ActualizarFuncionarioRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.CambiarEstadoFuncionarioRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.CambiarRolRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.CrearFuncionarioRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.FuncionarioResponse;
import co.edu.uniquindio.epq.seguridad.web.dto.RestablecerContrasenaRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.RolResponse;

/**
 * RF-14 / CU-10: administración de cuentas y roles de funcionarios (solo Administrador).
 */
public interface AdministracionFuncionariosService {

    List<FuncionarioResponse> listar(Rol rol, Boolean activo);

    FuncionarioResponse obtener(Integer id);

    FuncionarioResponse crear(CrearFuncionarioRequest solicitud);

    FuncionarioResponse actualizar(Integer id, ActualizarFuncionarioRequest solicitud);

    FuncionarioResponse cambiarRol(Integer id, CambiarRolRequest solicitud);

    FuncionarioResponse cambiarEstado(Integer id, CambiarEstadoFuncionarioRequest solicitud);

    void restablecerContrasena(Integer id, RestablecerContrasenaRequest solicitud);

    List<RolResponse> roles();
}
