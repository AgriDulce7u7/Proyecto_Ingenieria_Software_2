package co.edu.uniquindio.epq.comercial.dominio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import co.edu.uniquindio.epq.comun.territorio.Municipio;
import co.edu.uniquindio.epq.seguridad.dominio.ControlAcceso;
import co.edu.uniquindio.epq.seguridad.servicio.CuentaCiudadano;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
 * Usuario del servicio (F-03): persona natural o jurídica titular de uno o más contratos.
 * También es la cuenta del portal de autogestión (RF-07).
 */
@Entity
@Table(name = "cliente")
public class Cliente implements CuentaCiudadano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo_interno", unique = true, length = 20, updatable = false)
    private String codigoInterno;

    @Column(name = "tipo_documento", nullable = false, length = 5)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_persona", nullable = false, length = 10)
    private TipoPersona tipoPersona = TipoPersona.NATURAL;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(length = 150)
    private String correo;

    @Column(length = 20)
    private String telefono;

    @Column(name = "direccion_notificacion", length = 200)
    private String direccionNotificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;

    @Convert(converter = EstadoValidacionIdentidadConverter.class)
    @Column(name = "estado_validacion", nullable = false, length = 20)
    private EstadoValidacionIdentidad estadoValidacion = EstadoValidacionIdentidad.PENDIENTE;

    @Column(name = "detalle_validacion", length = 300)
    private String detalleValidacion;

    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "registrado_por", length = 150, updatable = false)
    private String registradoPor;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Embedded
    private ControlAcceso controlAcceso = new ControlAcceso();

    protected Cliente() {
        // Requerido por JPA
    }

    /** Registro de un nuevo usuario del servicio (CO-03 registrarUsuario). */
    public static Cliente registrar(String codigoInterno, String tipoDocumento, String numeroDocumento,
                                    TipoPersona tipoPersona, String nombreCompleto, String correo, String telefono,
                                    String direccionNotificacion, Municipio municipio, String registradoPor,
                                    LocalDateTime ahora) {
        Cliente c = new Cliente();
        c.codigoInterno = Objects.requireNonNull(codigoInterno);
        c.tipoDocumento = Objects.requireNonNull(tipoDocumento);
        c.numeroDocumento = Objects.requireNonNull(numeroDocumento).trim();
        c.tipoPersona = tipoPersona == null ? TipoPersona.NATURAL : tipoPersona;
        c.nombreCompleto = Objects.requireNonNull(nombreCompleto).trim();
        c.correo = Objects.requireNonNull(correo).trim().toLowerCase();
        c.telefono = Objects.requireNonNull(telefono).trim();
        c.direccionNotificacion = Objects.requireNonNull(direccionNotificacion).trim();
        c.municipio = Objects.requireNonNull(municipio);
        c.registradoPor = registradoPor;
        c.fechaRegistro = ahora;
        c.activo = true;
        c.estadoValidacion = EstadoValidacionIdentidad.PENDIENTE;
        return c;
    }

    /** Registra el resultado de la verificación contra la RNEC (IS-03). */
    public void registrarValidacion(EstadoValidacionIdentidad estado, String detalle, LocalDateTime fecha) {
        this.estadoValidacion = Objects.requireNonNull(estado);
        this.detalleValidacion = detalle;
        this.fechaValidacion = fecha;
    }

    /**
     * Actualiza los datos (RF-02) y retorna los cambios efectivos para la auditoría (SWR-15).
     * La identificación no se modifica: es la llave del usuario (SWR-12).
     */
    public List<CambioDato> actualizarDatos(String nombreCompleto, String correo, String telefono,
                                            String direccionNotificacion, Municipio municipio, LocalDateTime ahora) {
        List<CambioDato> cambios = new ArrayList<>();
        registrarCambio(cambios, "nombreCompleto", this.nombreCompleto, nombreCompleto.trim());
        registrarCambio(cambios, "correo", this.correo, correo.trim().toLowerCase());
        registrarCambio(cambios, "telefono", this.telefono, telefono.trim());
        registrarCambio(cambios, "direccionNotificacion", this.direccionNotificacion, direccionNotificacion.trim());
        registrarCambio(cambios, "municipio", nombre(this.municipio), nombre(municipio));
        this.nombreCompleto = nombreCompleto.trim();
        this.correo = correo.trim().toLowerCase();
        this.telefono = telefono.trim();
        this.direccionNotificacion = direccionNotificacion.trim();
        this.municipio = municipio;
        if (!cambios.isEmpty()) {
            this.fechaActualizacion = ahora;
        }
        return cambios;
    }

    /** El ciudadano actualiza sus datos de contacto desde el portal (Tabla 7). */
    public List<CambioDato> actualizarContacto(String correo, String telefono, String direccionNotificacion,
                                               LocalDateTime ahora) {
        return actualizarDatos(nombreCompleto, correo, telefono, direccionNotificacion, municipio, ahora);
    }

    /** Eliminación lógica (CU-01). */
    public void desactivar(LocalDateTime ahora) {
        this.activo = false;
        this.fechaActualizacion = ahora;
    }

    public void reactivar(LocalDateTime ahora) {
        this.activo = true;
        this.fechaActualizacion = ahora;
    }

    private static void registrarCambio(List<CambioDato> cambios, String campo, String anterior, String nuevo) {
        CambioDato cambio = new CambioDato(campo, anterior, nuevo);
        if (cambio.esCambio()) {
            cambios.add(cambio);
        }
    }

    private static String nombre(Municipio municipio) {
        return municipio == null ? null : municipio.getNombre();
    }

    // ------------------------------------------------------------------ cuenta del portal (RF-07)

    @Override
    public boolean tieneCuentaPortal() {
        return passwordHash != null;
    }

    @Override
    public void definirContrasenaPortal(String hash) {
        this.passwordHash = Objects.requireNonNull(hash);
        controlAcceso().reiniciar();
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
    public Integer municipioId() {
        return municipio == null ? null : municipio.getId();
    }

    // ------------------------------------------------------------------ getters

    @Override
    public Integer getId() {
        return id;
    }

    public String getCodigoInterno() {
        return codigoInterno;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    @Override
    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public TipoPersona getTipoPersona() {
        return tipoPersona;
    }

    @Override
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    @Override
    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDireccionNotificacion() {
        return direccionNotificacion;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public EstadoValidacionIdentidad getEstadoValidacion() {
        return estadoValidacion;
    }

    public String getDetalleValidacion() {
        return detalleValidacion;
    }

    public LocalDateTime getFechaValidacion() {
        return fechaValidacion;
    }

    @Override
    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    @Override
    public String getPasswordHash() {
        return passwordHash;
    }
}
