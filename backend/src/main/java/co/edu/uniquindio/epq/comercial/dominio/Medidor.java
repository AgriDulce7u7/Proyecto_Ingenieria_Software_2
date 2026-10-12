package co.edu.uniquindio.epq.comercial.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

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
 * Medidor instalado para un contrato. Al reemplazarlo, el anterior queda "retirado".
 */
@Entity
@Table(name = "medidor")
public class Medidor {

    public static final String ESTADO_ACTIVO = "activo";
    public static final String ESTADO_RETIRADO = "retirado";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(name = "numero_serie", nullable = false, unique = true, length = 50)
    private String numeroSerie;

    @Column(name = "fecha_instalacion", nullable = false)
    private LocalDate fechaInstalacion;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "fecha_retiro")
    private LocalDateTime fechaRetiro;

    protected Medidor() {
        // Requerido por JPA
    }

    public static Medidor instalar(Contrato contrato, String numeroSerie, LocalDate fechaInstalacion) {
        Medidor m = new Medidor();
        m.contrato = Objects.requireNonNull(contrato);
        m.numeroSerie = Objects.requireNonNull(numeroSerie).trim().toUpperCase();
        m.fechaInstalacion = Objects.requireNonNull(fechaInstalacion);
        m.estado = ESTADO_ACTIVO;
        return m;
    }

    public void retirar(LocalDateTime ahora) {
        this.estado = ESTADO_RETIRADO;
        this.fechaRetiro = ahora;
    }

    public boolean estaActivo() {
        return ESTADO_ACTIVO.equals(estado);
    }

    public Integer getId() {
        return id;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public LocalDate getFechaInstalacion() {
        return fechaInstalacion;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFechaRetiro() {
        return fechaRetiro;
    }
}
