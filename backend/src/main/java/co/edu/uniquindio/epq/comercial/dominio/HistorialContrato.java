package co.edu.uniquindio.epq.comercial.dominio;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Historial de estados del contrato con motivo, fecha y responsable (SyR-04d, SWR-20).
 */
@Entity
@Table(name = "historial_contrato")
public class HistorialContrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Convert(converter = EstadoContratoConverter.class)
    @Column(name = "estado_anterior", length = 20)
    private EstadoContrato estadoAnterior;

    @Convert(converter = EstadoContratoConverter.class)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoContrato estadoNuevo;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(nullable = false, length = 150)
    private String usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected HistorialContrato() {
        // Requerido por JPA
    }

    public static HistorialContrato registrar(Contrato contrato, EstadoContrato anterior, String motivo,
                                              String usuario, LocalDateTime fecha) {
        HistorialContrato h = new HistorialContrato();
        h.contrato = contrato;
        h.estadoAnterior = anterior;
        h.estadoNuevo = contrato.getEstado();
        h.motivo = motivo;
        h.usuario = usuario;
        h.fecha = fecha;
        return h;
    }

    public Long getId() {
        return id;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public EstadoContrato getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoContrato getEstadoNuevo() {
        return estadoNuevo;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getUsuario() {
        return usuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
