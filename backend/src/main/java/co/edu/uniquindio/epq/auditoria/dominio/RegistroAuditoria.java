package co.edu.uniquindio.epq.auditoria.dominio;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entrada inmutable del log de auditoría (RF-15). No tiene métodos de modificación.
 */
@Entity
@Table(name = "log_auditoria")
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 150, updatable = false)
    private String usuario;

    @Column(length = 40, updatable = false)
    private String rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40, updatable = false)
    private ModuloAuditoria modulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60, updatable = false)
    private AccionAuditoria accion;

    @Column(length = 60, updatable = false)
    private String entidad;

    @Column(name = "entidad_id", length = 60, updatable = false)
    private String entidadId;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String detalle;

    @Column(nullable = false, updatable = false)
    private boolean exitoso;

    @Column(name = "ip_origen", length = 60, updatable = false)
    private String ipOrigen;

    protected RegistroAuditoria() {
        // Requerido por JPA
    }

    public static RegistroAuditoria de(LocalDateTime fecha, String usuario, String rol, AccionAuditoria accion,
                                       String entidad, String entidadId, String detalle, boolean exitoso,
                                       String ipOrigen) {
        RegistroAuditoria r = new RegistroAuditoria();
        r.fecha = fecha;
        r.usuario = usuario;
        r.rol = rol;
        r.modulo = accion.modulo();
        r.accion = accion;
        r.entidad = entidad;
        r.entidadId = entidadId;
        r.detalle = detalle;
        r.exitoso = exitoso;
        r.ipOrigen = ipOrigen;
        return r;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getRol() {
        return rol;
    }

    public ModuloAuditoria getModulo() {
        return modulo;
    }

    public AccionAuditoria getAccion() {
        return accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public String getEntidadId() {
        return entidadId;
    }

    public String getDetalle() {
        return detalle;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }
}
