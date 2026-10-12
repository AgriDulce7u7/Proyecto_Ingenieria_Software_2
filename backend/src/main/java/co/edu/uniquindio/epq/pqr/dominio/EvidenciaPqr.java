package co.edu.uniquindio.epq.pqr.dominio;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Documento de soporte de la respuesta de una PQR (RN-06, CU-06 paso 9).
 */
@Entity
@Table(name = "evidencia_pqr")
public class EvidenciaPqr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pqr_id", nullable = false, updatable = false)
    private Pqr pqr;

    @Column(name = "nombre_archivo", nullable = false, length = 200, updatable = false)
    private String nombreArchivo;

    @Column(name = "tipo_contenido", nullable = false, length = 100, updatable = false)
    private String tipoContenido;

    @Column(name = "tamano_bytes", nullable = false, updatable = false)
    private int tamanoBytes;

    /** PostgreSQL BYTEA. Las consultas de listado usan una proyección para no cargar el contenido. */
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, updatable = false)
    private byte[] contenido;

    @Column(length = 300, updatable = false)
    private String descripcion;

    @Column(name = "fecha_carga", nullable = false, updatable = false)
    private LocalDateTime fechaCarga;

    @Column(nullable = false, length = 150, updatable = false)
    private String usuario;

    protected EvidenciaPqr() {
        // Requerido por JPA
    }

    public static EvidenciaPqr adjuntar(Pqr pqr, String nombreArchivo, String tipoContenido, byte[] contenido,
                                        String descripcion, String usuario, LocalDateTime fecha) {
        EvidenciaPqr e = new EvidenciaPqr();
        e.pqr = Objects.requireNonNull(pqr);
        e.nombreArchivo = Objects.requireNonNull(nombreArchivo);
        e.tipoContenido = Objects.requireNonNull(tipoContenido);
        e.contenido = Objects.requireNonNull(contenido);
        e.tamanoBytes = contenido.length;
        e.descripcion = descripcion;
        e.usuario = usuario;
        e.fechaCarga = fecha;
        return e;
    }

    public Long getId() {
        return id;
    }

    public Pqr getPqr() {
        return pqr;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public int getTamanoBytes() {
        return tamanoBytes;
    }

    public byte[] getContenido() {
        return contenido;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public String getUsuario() {
        return usuario;
    }
}
