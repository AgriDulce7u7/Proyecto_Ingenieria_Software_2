package co.edu.uniquindio.epq.auditoria.servicio.impl;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.dominio.RegistroAuditoria;
import co.edu.uniquindio.epq.auditoria.repositorio.RegistroAuditoriaRepository;
import co.edu.uniquindio.epq.auditoria.servicio.ServicioAuditoria;
import co.edu.uniquindio.epq.seguridad.contexto.ContextoSeguridad;
import co.edu.uniquindio.epq.seguridad.contexto.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class ServicioAuditoriaJpa implements ServicioAuditoria {

    private static final int MAX_DETALLE = 4000;

    private final RegistroAuditoriaRepository repositorio;
    private final ContextoSeguridad contexto;
    private final Clock clock;

    public ServicioAuditoriaJpa(RegistroAuditoriaRepository repositorio, ContextoSeguridad contexto, Clock clock) {
        this.repositorio = repositorio;
        this.contexto = contexto;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void registrar(AccionAuditoria accion, String entidad, Object entidadId, String detalle) {
        String rol = contexto.usuarioActual().map(UsuarioAutenticado::rol).map(Enum::name).orElse(null);
        guardar(contexto.identificadorActual(), rol, accion, entidad, entidadId, detalle, true);
    }

    @Override
    @Transactional
    public void registrarComo(String usuario, String rol, AccionAuditoria accion, String entidad, Object entidadId,
                              String detalle, boolean exitoso) {
        guardar(usuario, rol, accion, entidad, entidadId, detalle, exitoso);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarIndependiente(String usuario, String rol, AccionAuditoria accion, String entidad,
                                       Object entidadId, String detalle, boolean exitoso) {
        guardar(usuario, rol, accion, entidad, entidadId, detalle, exitoso);
    }

    private void guardar(String usuario, String rol, AccionAuditoria accion, String entidad, Object entidadId,
                         String detalle, boolean exitoso) {
        repositorio.save(RegistroAuditoria.de(LocalDateTime.now(clock),
                usuario == null || usuario.isBlank() ? ContextoSeguridad.USUARIO_SISTEMA : usuario,
                rol, accion, entidad, entidadId == null ? null : String.valueOf(entidadId),
                recortar(detalle), exitoso, ipOrigen()));
    }

    private static String recortar(String detalle) {
        return detalle == null || detalle.length() <= MAX_DETALLE ? detalle : detalle.substring(0, MAX_DETALLE);
    }

    /** IP del cliente; considera el encabezado X-Forwarded-For cuando hay un proxy inverso. */
    private static String ipOrigen() {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        if (!(atributos instanceof ServletRequestAttributes servlet)) {
            return null; // tarea programada: no hay petición HTTP
        }
        HttpServletRequest peticion = servlet.getRequest();
        String reenviada = peticion.getHeader("X-Forwarded-For");
        String ip = reenviada != null && !reenviada.isBlank() ? reenviada.split(",")[0].trim() : peticion.getRemoteAddr();
        return ip != null && ip.length() > 60 ? ip.substring(0, 60) : ip;
    }
}
