package co.edu.uniquindio.epq.pqr.dominio;

import java.time.LocalDateTime;

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
 * Registro de auditoría de la PQR (SWR-09): creación, asignación, trámite y respuesta.
 */
@Entity
@Table(name = "historial_pqr")
public class HistorialPqr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pqr_id", nullable = false)
    private Pqr pqr;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoPqr estado;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "usuario_cambio", length = 150)
    private String usuarioCambio;

    protected HistorialPqr() {
        // Requerido por JPA
    }

    public static HistorialPqr registrar(Pqr pqr, String comentario, String usuario, LocalDateTime fecha) {
        HistorialPqr historial = new HistorialPqr();
        historial.pqr = pqr;
        historial.estado = pqr.getEstado();
        historial.comentario = comentario;
        historial.usuarioCambio = usuario;
        historial.fechaCambio = fecha;
        return historial;
    }

    public Long getId() {
        return id;
    }

    public Pqr getPqr() {
        return pqr;
    }

    public EstadoPqr getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }

    public String getComentario() {
        return comentario;
    }

    public String getUsuarioCambio() {
        return usuarioCambio;
    }
}
