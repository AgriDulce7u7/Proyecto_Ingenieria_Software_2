package co.edu.uniquindio.epq.comercial.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;

import co.edu.uniquindio.epq.comun.territorio.Municipio;
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
 * Tarifa de un servicio con su periodo de vigencia (SWR-03).
 */
@Entity
@Table(name = "tarifa")
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "valor_por_unidad", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorPorUnidad;

    @Column(name = "cargo_fijo", nullable = false, precision = 12, scale = 2)
    private BigDecimal cargoFijo;

    @Column(name = "fecha_vigencia_inicio", nullable = false)
    private LocalDate fechaVigenciaInicio;

    @Column(name = "fecha_vigencia_fin")
    private LocalDate fechaVigenciaFin;

    /** Municipio al que aplica; {@code null} = todos (RN-08). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;

    /** Estrato al que aplica; {@code null} = todos (RN-08). */
    @Column
    private Short estrato;

    protected Tarifa() {
        // Requerido por JPA
    }

    /** Una tarifa está vigente si la fecha está dentro de [inicio, fin] (fin nulo = sin límite). */
    public boolean estaVigenteEn(LocalDate fecha) {
        boolean iniciada = !fecha.isBefore(fechaVigenciaInicio);
        boolean noFinalizada = fechaVigenciaFin == null || !fecha.isAfter(fechaVigenciaFin);
        return iniciada && noFinalizada;
    }

    /**
     * RN-08 / SWR-18: la tarifa aplica si sus criterios (municipio y estrato) son nulos o coinciden
     * con los del predio.
     */
    public boolean esAplicableA(Integer municipioId, Integer estratoPredio) {
        boolean municipioCoincide = municipio == null || municipio.getId().equals(municipioId);
        boolean estratoCoincide = estrato == null || (estratoPredio != null && estrato.intValue() == estratoPredio);
        return municipioCoincide && estratoCoincide;
    }

    /** Entre más criterios defina, más específica es: municipio pesa 2 y estrato 1. */
    public int especificidad() {
        return (municipio != null ? 2 : 0) + (estrato != null ? 1 : 0);
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public Integer getEstrato() {
        return estrato == null ? null : estrato.intValue();
    }

    public Integer getId() {
        return id;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getValorPorUnidad() {
        return valorPorUnidad;
    }

    public BigDecimal getCargoFijo() {
        return cargoFijo;
    }

    public LocalDate getFechaVigenciaInicio() {
        return fechaVigenciaInicio;
    }

    public LocalDate getFechaVigenciaFin() {
        return fechaVigenciaFin;
    }
}
