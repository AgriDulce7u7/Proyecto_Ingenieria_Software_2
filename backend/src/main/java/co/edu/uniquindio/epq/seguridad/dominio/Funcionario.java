package co.edu.uniquindio.epq.seguridad.dominio;

import java.time.LocalDateTime;
import java.util.Objects;

import co.edu.uniquindio.epq.comun.territorio.Municipio;
import co.edu.uniquindio.epq.seguridad.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Cuenta de un funcionario de EPQ (RF-14). Tiene un único rol (principio de mínimo privilegio).
 */
@Entity
@Table(name = "funcionario")
public class Funcionario implements CuentaConCredenciales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 60)
    private String usuario;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;

    /** Registro de gestor de PQR vinculado (solo para GESTOR_PQR y JEFE_PQR). */
    @Column(name = "gestor_id", unique = true)
    private Integer gestorId;

    @Column(nullable = false)
    private boolean activo;

    @Embedded
    private ControlAcceso controlAcceso = new ControlAcceso();

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    protected Funcionario() {
        // Requerido por JPA
    }

    public static Funcionario crear(String usuario, String nombreCompleto, String correo, String passwordHash, Rol rol,
                                    Municipio municipio, Integer gestorId, LocalDateTime ahora) {
        Funcionario f = new Funcionario();
        f.usuario = Objects.requireNonNull(usuario).trim().toLowerCase();
        f.nombreCompleto = Objects.requireNonNull(nombreCompleto).trim();
        f.correo = Objects.requireNonNull(correo).trim().toLowerCase();
        f.passwordHash = Objects.requireNonNull(passwordHash);
        f.municipio = municipio;
        f.asignarRol(rol, gestorId);
        f.activo = true;
        f.fechaCreacion = ahora;
        return f;
    }

    public void actualizarDatos(String nombreCompleto, String correo, Municipio municipio, LocalDateTime ahora) {
        this.nombreCompleto = Objects.requireNonNull(nombreCompleto).trim();
        this.correo = Objects.requireNonNull(correo).trim().toLowerCase();
        this.municipio = municipio;
        this.fechaActualizacion = ahora;
    }

    public void cambiarRol(Rol nuevoRol, Integer nuevoGestorId, LocalDateTime ahora) {
        asignarRol(nuevoRol, nuevoGestorId);
        this.fechaActualizacion = ahora;
    }

    private void asignarRol(Rol nuevoRol, Integer nuevoGestorId) {
        Objects.requireNonNull(nuevoRol, "El rol es obligatorio");
        if (!nuevoRol.esInterno()) {
            throw new IllegalArgumentException("Un funcionario no puede tener el rol " + nuevoRol);
        }
        if (nuevoRol.requiereGestorPqr() && nuevoGestorId == null) {
            throw new IllegalArgumentException("El rol " + nuevoRol + " requiere el gestor de PQR vinculado (gestorId)");
        }
        this.rol = nuevoRol;
        this.gestorId = nuevoRol.requiereGestorPqr() ? nuevoGestorId : null;
    }

    public void activar(LocalDateTime ahora) {
        this.activo = true;
        controlAcceso().reiniciar();
        this.fechaActualizacion = ahora;
    }

    public void desactivar(LocalDateTime ahora) {
        this.activo = false;
        this.fechaActualizacion = ahora;
    }

    public void cambiarPassword(String nuevoHash, LocalDateTime ahora) {
        this.passwordHash = Objects.requireNonNull(nuevoHash);
        controlAcceso().reiniciar();
        this.fechaActualizacion = ahora;
    }

    @Override
    public boolean puedeIniciarSesion() {
        return activo && passwordHash != null;
    }

    @Override
    public ControlAcceso controlAcceso() {
        if (controlAcceso == null) {
            controlAcceso = new ControlAcceso(); // Hibernate deja null un @Embedded cuyas columnas son todas nulas
        }
        return controlAcceso;
    }

    @Override
    public void registrarAccesoExitoso(LocalDateTime ahora) {
        controlAcceso().reiniciar();
        this.ultimoAcceso = ahora;
    }

    @Override
    public String getPasswordHash() {
        return passwordHash;
    }

    public Integer getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public Rol getRol() {
        return rol;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public Integer getGestorId() {
        return gestorId;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
