package co.edu.uniquindio.epq.seguridad.servicio.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.comun.excepcion.RecursoDuplicadoException;
import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.territorio.Municipio;
import co.edu.uniquindio.epq.comun.territorio.ResolutorMunicipios;
import co.edu.uniquindio.epq.seguridad.Rol;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;
import co.edu.uniquindio.epq.seguridad.dominio.Funcionario;
import co.edu.uniquindio.epq.seguridad.repositorio.FuncionarioRepository;
import co.edu.uniquindio.epq.seguridad.servicio.AdministracionFuncionariosService;
import co.edu.uniquindio.epq.seguridad.web.dto.ActualizarFuncionarioRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.CambiarEstadoFuncionarioRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.CambiarRolRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.CrearFuncionarioRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.FuncionarioResponse;
import co.edu.uniquindio.epq.seguridad.web.dto.RestablecerContrasenaRequest;
import co.edu.uniquindio.epq.seguridad.web.dto.RolResponse;

@Service
@Transactional
public class AdministracionFuncionariosServiceImpl implements AdministracionFuncionariosService {

    private static final String ENTIDAD = "funcionario";

    private final FuncionarioRepository repositorio;
    private final ResolutorMunicipios municipios;
    private final PasswordEncoder codificador;
    private final ServicioAuditoria auditoria;
    private final ContextoSeguridad contexto;
    private final Clock clock;

    public AdministracionFuncionariosServiceImpl(FuncionarioRepository repositorio, ResolutorMunicipios municipios,
                                                 PasswordEncoder codificador, ServicioAuditoria auditoria,
                                                 ContextoSeguridad contexto, Clock clock) {
        this.repositorio = repositorio;
        this.municipios = municipios;
        this.codificador = codificador;
        this.auditoria = auditoria;
        this.contexto = contexto;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuncionarioResponse> listar(Rol rol, Boolean activo) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        return repositorio.findAllByOrderByNombreCompletoAsc().stream()
                .filter(f -> rol == null || f.getRol() == rol)
                .filter(f -> activo == null || f.isActivo() == activo)
                .map(f -> FuncionarioResponse.de(f, ahora))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FuncionarioResponse obtener(Integer id) {
        return FuncionarioResponse.de(buscar(id), LocalDateTime.now(clock));
    }

    @Override
    public FuncionarioResponse crear(CrearFuncionarioRequest s) {
        if (repositorio.existsByUsuarioIgnoreCase(s.usuario().trim())) {
            throw new RecursoDuplicadoException("Ya existe un funcionario con el usuario '" + s.usuario() + "'");
        }
        if (repositorio.existsByCorreoIgnoreCase(s.correo().trim())) {
            throw new RecursoDuplicadoException("Ya existe un funcionario con el correo '" + s.correo() + "'");
        }
        validarGestorLibre(s.rol(), s.gestorId(), null);
        LocalDateTime ahora = LocalDateTime.now(clock);
        Funcionario funcionario = repositorio.save(Funcionario.crear(s.usuario(), s.nombreCompleto(), s.correo(),
                codificador.encode(s.contrasenaTemporal()), validarRol(s.rol()), municipio(s.municipioId()),
                s.gestorId(), ahora));
        auditoria.registrar(AccionAuditoria.CREAR_FUNCIONARIO, ENTIDAD, funcionario.getId(),
                "Cuenta creada: usuario=%s, rol=%s".formatted(funcionario.getUsuario(), funcionario.getRol()));
        return FuncionarioResponse.de(funcionario, ahora);
    }

    @Override
    public FuncionarioResponse actualizar(Integer id, ActualizarFuncionarioRequest s) {
        Funcionario funcionario = buscar(id);
        if (!funcionario.getCorreo().equalsIgnoreCase(s.correo().trim())
                && repositorio.existsByCorreoIgnoreCase(s.correo().trim())) {
            throw new RecursoDuplicadoException("Ya existe un funcionario con el correo '" + s.correo() + "'");
        }
        String antes = "nombre=%s, correo=%s".formatted(funcionario.getNombreCompleto(), funcionario.getCorreo());
        LocalDateTime ahora = LocalDateTime.now(clock);
        funcionario.actualizarDatos(s.nombreCompleto(), s.correo(), municipio(s.municipioId()), ahora);
        auditoria.registrar(AccionAuditoria.MODIFICAR_FUNCIONARIO, ENTIDAD, id,
                "Antes: %s | Después: nombre=%s, correo=%s".formatted(antes, funcionario.getNombreCompleto(),
                        funcionario.getCorreo()));
        return FuncionarioResponse.de(funcionario, ahora);
    }

    @Override
    public FuncionarioResponse cambiarRol(Integer id, CambiarRolRequest s) {
        Funcionario funcionario = buscar(id);
        Rol anterior = funcionario.getRol();
        Rol nuevo = validarRol(s.rol());
        if (anterior == Rol.ADMINISTRADOR && nuevo != Rol.ADMINISTRADOR) {
            protegerUltimoAdministrador(funcionario);
        }
        validarGestorLibre(nuevo, s.gestorId(), funcionario.getId());
        LocalDateTime ahora = LocalDateTime.now(clock);
        funcionario.cambiarRol(nuevo, s.gestorId(), ahora);
        auditoria.registrar(AccionAuditoria.CAMBIAR_ROL, ENTIDAD, id,
                "Rol %s → %s. Motivo: %s".formatted(anterior, nuevo, s.motivo().trim()));
        return FuncionarioResponse.de(funcionario, ahora);
    }

    @Override
    public FuncionarioResponse cambiarEstado(Integer id, CambiarEstadoFuncionarioRequest s) {
        Funcionario funcionario = buscar(id);
        LocalDateTime ahora = LocalDateTime.now(clock);
        if (Boolean.TRUE.equals(s.activo())) {
            funcionario.activar(ahora);
        } else {
            if (contexto.usuarioActual().map(u -> u.id().equals(id) && !u.esCiudadano()).orElse(false)) {
                throw new ReglaNegocioException("Un administrador no puede desactivar su propia cuenta");
            }
            if (funcionario.getRol() == Rol.ADMINISTRADOR) {
                protegerUltimoAdministrador(funcionario);
            }
            funcionario.desactivar(ahora);
        }
        auditoria.registrar(AccionAuditoria.CAMBIAR_ESTADO_FUNCIONARIO, ENTIDAD, id,
                "%s. Motivo: %s".formatted(funcionario.isActivo() ? "Cuenta activada" : "Cuenta desactivada",
                        s.motivo().trim()));
        return FuncionarioResponse.de(funcionario, ahora);
    }

    @Override
    public void restablecerContrasena(Integer id, RestablecerContrasenaRequest s) {
        Funcionario funcionario = buscar(id);
        funcionario.cambiarPassword(codificador.encode(s.nuevaContrasena()), LocalDateTime.now(clock));
        auditoria.registrar(AccionAuditoria.RESTABLECER_CONTRASENA, ENTIDAD, id,
                "Contraseña restablecida y bloqueo reiniciado para " + funcionario.getUsuario());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolResponse> roles() {
        return Arrays.stream(Rol.values())
                .map(r -> new RolResponse(r.name(), r.getDescripcion(), r.esInterno(), r.requiereGestorPqr(), r.getPermisos()))
                .toList();
    }

    private Funcionario buscar(Integer id) {
        return repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Funcionario", id));
    }

    private Municipio municipio(Integer municipioId) {
        return municipioId == null ? null : municipios.atendido(municipioId);
    }

    private static Rol validarRol(Rol rol) {
        if (!rol.esInterno()) {
            throw new ReglaNegocioException("El rol CIUDADANO no se asigna a funcionarios");
        }
        return rol;
    }

    private void validarGestorLibre(Rol rol, Integer gestorId, Integer funcionarioId) {
        if (!rol.requiereGestorPqr()) {
            return;
        }
        if (gestorId == null) {
            throw new ReglaNegocioException("El rol " + rol + " requiere el gestor de PQR vinculado (gestorId)");
        }
        boolean ocupado = repositorio.findByGestorId(gestorId)
                .map(f -> !f.getId().equals(funcionarioId))
                .orElse(false);
        if (ocupado) {
            throw new RecursoDuplicadoException("El gestor " + gestorId + " ya está vinculado a otra cuenta");
        }
    }

    /** Evita dejar el sistema sin administradores activos. */
    private void protegerUltimoAdministrador(Funcionario funcionario) {
        if (funcionario.isActivo() && repositorio.countByRolAndActivoTrue(Rol.ADMINISTRADOR) <= 1) {
            throw new ReglaNegocioException("No se puede retirar el único administrador activo del sistema");
        }
    }
}
