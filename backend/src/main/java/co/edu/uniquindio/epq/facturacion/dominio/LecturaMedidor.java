package co.edu.uniquindio.epq.facturacion.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

import co.edu.uniquindio.epq.comun.persistencia.PeriodoConverter;
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
 * Lectura mensual de un medidor. Es la fuente del consumo facturado (SWR-03).
 */
@Entity
@Table(name = "lectura_medidor")
public class LecturaMedidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medidor_id", nullable = false)
    private Medidor medidor;

    @Convert(converter = PeriodoConverter.class)
    @Column(nullable = false, length = 7, columnDefinition = "char(7)")
    private YearMonth periodo;

    @Column(name = "lectura_anterior", nullable = false, precision = 12, scale = 2)
    private BigDecimal lecturaAnterior;

    @Column(name = "lectura_actual", nullable = false, precision = 12, scale = 2)
    private BigDecimal lecturaActual;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal consumo;

    @Column(name = "fecha_lectura", nullable = false)
    private LocalDateTime fechaLectura;

    protected LecturaMedidor() {
        // Requerido por JPA
    }

    public Long getId() {
        return id;
    }

    public Medidor getMedidor() {
        return medidor;
    }

    public YearMonth getPeriodo() {
        return periodo;
    }

    public BigDecimal getLecturaAnterior() {
        return lecturaAnterior;
    }

    public BigDecimal getLecturaActual() {
        return lecturaActual;
    }

    public BigDecimal getConsumo() {
        return consumo;
    }

    public LocalDateTime getFechaLectura() {
        return fechaLectura;
    }
}
