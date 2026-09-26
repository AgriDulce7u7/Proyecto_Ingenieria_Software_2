package co.edu.uniquindio.epq.facturacion.dominio;

import java.math.BigDecimal;
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

    protected Tarifa() {
        // Requerido por JPA
    }

    /** Una tarifa está vigente si la fecha está dentro de [inicio, fin] (fin nulo = sin límite). */
    public boolean estaVigenteEn(LocalDate fecha) {
        boolean iniciada = !fecha.isBefore(fechaVigenciaInicio);
        boolean noFinalizada = fechaVigenciaFin == null || !fecha.isAfter(fechaVigenciaFin);
        return iniciada && noFinalizada;
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
