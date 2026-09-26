package co.edu.uniquindio.epq.facturacion.dominio;

import java.time.LocalDate;

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
 * Medidor instalado para un contrato.
 */
@Entity
@Table(name = "medidor")
public class Medidor {

    public static final String ESTADO_ACTIVO = "activo";

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

    protected Medidor() {
        // Requerido por JPA
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
}
