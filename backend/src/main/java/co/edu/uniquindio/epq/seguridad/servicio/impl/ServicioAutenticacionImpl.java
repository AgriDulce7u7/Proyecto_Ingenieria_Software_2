package co.edu.uniquindio.epq.seguridad.servicio.impl;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.comun.excepcion.RecursoDuplicadoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.seguridad.Rol;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;
import co.edu.uniquindio.epq.seguridad.contexto.TipoUsuario;
import co.edu.uniquindio.epq.seguridad.contexto.UsuarioAutenticado;
import co.edu.uniquindio.epq.seguridad.dominio.Funcionario;
import co.edu.uniquindio.epq.seguridad.repositorio.FuncionarioRepository;
import co.edu.uniquindio.epq.seguridad.servicio.CredencialesInvalidasException;
import co.edu.uniquindio.epq.seguridad.servicio.CuentaBloqueadaException;
import co.edu.uniquindio.epq.seguridad.servicio.CuentaCiudadano;
import co.edu.uniquindio.epq.seguridad.servicio.RepositorioCuentasCiudadano;
import co.edu.uniquindio.epq.seguridad.servicio.ServicioAutenticacion;
import co.edu.uniquindio.epq.seguridad.servicio.VerificadorCredenciales;
import co.edu.uniquindio.epq.seguridad.token.ProveedorToken;
import co.edu.uniquindio.epq.seguridad.token.TokenEmitido;
import co.edu.uniquindio.epq.seguridad.web.dto.ActivarCuentaCiudadanoRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.LoginCiudadanoRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.LoginRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.SesionResponse;

/**
 * Autenticación con auditoría de cada intento (RF-15: inicio/cierre de sesión).
 *
 * <p>Los métodos de inicio de sesión no revierten la transacción ante credenciales inválidas: así
 * se guardan el contador de intentos fallidos, el bloqueo y el registro de auditoría.</p>
 */
@Service
public class ServicioAutenticacionImpl implements ServicioAutenticacion {

    private final FuncionarioRepository funcionarioRepository;
    private final RepositorioCuentasCiudadano cuentasCiudadano;
    private final VerificadorCredenciales verificador;
    private final ProveedorToken proveedorToken;
    private final PasswordEncoder codificador;
    private final ServicioAuditoria auditoria;
    private final ContextoSeguridad contexto;
    private final Clock clock;

    public ServicioAutenticacionImpl(FuncionarioRepository funcionarioRepository,
                                     RepositorioCuentasCiudadano cuentasCiudadano, VerificadorCredenciales verificador,
                                     ProveedorToken proveedorToken, PasswordEncoder codificador,
                                     ServicioAuditoria auditoria, ContextoSeguridad contexto, Clock clock) {
        this.funcionarioRepository = funcionarioRepository;
        this.cuentasCiudadano = cuentasCiudadano;
        this.verificador = verificador;
        this.proveedorToken = proveedorToken;
        this.codificador = codificador;
        this.auditoria = auditoria;
        this.contexto = contexto;
        this.clock = clock;
    }

    @Override
    @Transactional(noRollbackFor = {CredencialesInvalidasException.class, CuentaBloqueadaException.class})
    public SesionResponse iniciarSesionFuncionario(LoginRequest solicitud) {
        String usuario = solicitud.usuario().trim().toLowerCase();
        Funcionario funcionario = funcionarioRepository.findByUsuarioIgnoreCase(usuario).orElse(null);
        verificarYAuditar(funcionario, solicitud.contrasena(), usuario,
                funcionario == null ? null : funcionario.getRol().name());

        UsuarioAutenticado autenticado = new UsuarioAutenticado(funcionario.getId(), funcionario.getUsuario(),
                funcionario.getNombreCompleto(), funcionario.getRol(), TipoUsuario.FUNCIONARIO,
                funcionario.getGestorId(), funcionario.getMunicipio() == null ? null : funcionario.getMunicipio().getId());
        auditoria.registrarComo(funcionario.getUsuario(), funcionario.getRol().name(), AccionAuditoria.INICIO_SESION,
                "funcionario", funcionario.getId(), "Inicio de sesión exitoso", true);
        return sesion(autenticado);
    }

    @Override
    @Transactional(noRollbackFor = {CredencialesInvalidasException.class, CuentaBloqueadaException.class})
    public SesionResponse iniciarSesionCiudadano(LoginCiudadanoRequest solicitud) {
        String documento = solicitud.numeroDocumento().trim();
        CuentaCiudadano cuenta = cuentasCiudadano.buscarPorNumeroDocumento(documento).orElse(null);
        String usuarioAuditoria = "ciudadano:" + documento;
        verificarYAuditar(cuenta, solicitud.contrasena(), usuarioAuditoria, Rol.CIUDADANO.name());

        UsuarioAutenticado autenticado = new UsuarioAutenticado(cuenta.getId(), cuenta.getNumeroDocumento(),
                cuenta.getNombreCompleto(), Rol.CIUDADANO, TipoUsuario.CIUDADANO, null, cuenta.municipioId());
        auditoria.registrarComo(usuarioAuditoria, Rol.CIUDADANO.name(), AccionAuditoria.INICIO_SESION,
                "cliente", cuenta.getId(), "Inicio de sesión en el portal de autogestión", true);
        return sesion(autenticado);
    }

    private void verificarYAuditar(co.edu.uniquindio.epq.seguridad.dominio.CuentaConCredenciales cuenta,
                                   String contrasena, String usuario, String rol) {
        try {
            verificador.verificar(cuenta, contrasena, LocalDateTime.now(clock));
        } catch (CuentaBloqueadaException ex) {
            auditoria.registrarComo(usuario, rol, AccionAuditoria.CUENTA_BLOQUEADA, null, null,
                    "Cuenta bloqueada por intentos fallidos hasta " + ex.propiedades().get("bloqueadoHasta"), false);
            throw ex;
        } catch (CredencialesInvalidasException ex) {
            auditoria.registrarComo(usuario, rol, AccionAuditoria.INICIO_SESION_FALLIDO, null, null,
                    "Credenciales inválidas", false);
            throw ex;
        }
    }

    @Override
    @Transactional
    public void activarCuentaCiudadano(ActivarCuentaCiudadanoRequest solicitud) {
        String documento = solicitud.numeroDocumento().trim();
        CuentaCiudadano cuenta = cuentasCiudadano.buscarPorNumeroDocumento(documento)
                .filter(CuentaCiudadano::isActivo)
                .filter(c -> c.getCorreo() != null && c.getCorreo().equalsIgnoreCase(solicitud.correo().trim()))
                .orElseThrow(() -> new ReglaNegocioException(
                        "Los datos no coinciden con un usuario del servicio activo. Verifique el número de "
                                + "identificación y el correo registrado, o acuda a una oficina de atención."));
        if (cuenta.tieneCuentaPortal()) {
            throw new RecursoDuplicadoException("El usuario ya tiene una cuenta en el portal de autogestión");
        }
        cuenta.definirContrasenaPortal(codificador.encode(solicitud.contrasena()));
        auditoria.registrarComo("ciudadano:" + documento, Rol.CIUDADANO.name(), AccionAuditoria.ACTIVAR_CUENTA_PORTAL,
                "cliente", cuenta.getId(), "Cuenta del portal de autogestión activada", true);
    }

    @Override
    @Transactional
    public void cerrarSesion() {
        auditoria.registrar(AccionAuditoria.CIERRE_SESION, null, null, "Cierre de sesión");
    }

    @Override
    public SesionResponse.PerfilResponse perfilActual() {
        return perfil(contexto.requerirUsuario());
    }

    private SesionResponse sesion(UsuarioAutenticado usuario) {
        TokenEmitido token = proveedorToken.emitir(usuario);
        return new SesionResponse(token.token(), "Bearer", token.expiraEn(), perfil(usuario));
    }

    private static SesionResponse.PerfilResponse perfil(UsuarioAutenticado u) {
        return new SesionResponse.PerfilResponse(u.id(), u.usuario(), u.nombre(), u.rol().name(),
                u.rol().getDescripcion(), u.tipo().name(), u.gestorId(), u.municipioId(), u.rol().getPermisos());
    }
}
