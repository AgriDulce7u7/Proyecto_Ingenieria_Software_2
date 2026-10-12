package co.edu.uniquindio.epq.cartera.dominio;

import java.time.LocalDateTime;

import co.edu.uniquindio.epq.comercial.dominio.Contrato;
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
 * Proceso de cobro coercitivo de un contrato en mora (RF-12, RN-07). Hay a lo sumo uno por contrato.
 */
@Entity
@Table(name = "proceso_cobro")
public class ProcesoCobro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, unique = true, updatable = false)
    private Contrato contrato;

    @Convert(converter = EstadoProcesoCobroConverter.class)
    @Column(nullable = false, length = 20)
    private EstadoProcesoCobro estado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(length = 500)
    private String motivo;

    protected ProcesoCobro() {
        // Requerido por JPA
    }

    public static ProcesoCobro iniciar(Contrato contrato, EstadoProcesoCobro estadoInicial, String motivo,
                                       LocalDateTime ahora) {
        ProcesoCobro p = new ProcesoCobro();
        p.contrato = contrato;
        p.estado = estadoInicial;
        p.motivo = motivo;
        p.fechaInicio = ahora;
        p.fechaActualizacion = ahora;
        return p;
    }

    /** RN-07: un acuerdo vigente suspende el cobro coercitivo. */
    public void suspender(String motivo, LocalDateTime ahora) {
        cambiar(EstadoProcesoCobro.SUSPENDIDO, motivo, ahora);
    }

    /** RN-07 / SWR-31: el incumplimiento reactiva el cobro coercitivo. También reabre un proceso cerrado. */
    public void reactivar(String motivo, LocalDateTime ahora) {
        if (estado == EstadoProcesoCobro.CERRADO) {
            this.fechaInicio = ahora;
        }
        cambiar(EstadoProcesoCobro.ACTIVO, motivo, ahora);
    }

    public void cerrar(String motivo, LocalDateTime ahora) {
        cambiar(EstadoProcesoCobro.CERRADO, motivo, ahora);
    }

    private void cambiar(EstadoProcesoCobro nuevo, String motivo, LocalDateTime ahora) {
        this.estado = nuevo;
        this.motivo = motivo;
        this.fechaActualizacion = ahora;
    }

    public Integer getId() {
        return id;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public EstadoProcesoCobro getEstado() {
        return estado;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public String getMotivo() {
        return motivo;
    }
}
