package co.edu.uniquindio.epq.facturacion.dominio;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.persistencia.PeriodoConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Ejecución masiva de facturación de un periodo (SWR-01, SWR-02). Hay un único lote por periodo.
 */
@Entity
@Table(name = "lote_facturacion")
public class LoteFacturacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Convert(converter = PeriodoConverter.class)
    @Column(nullable = false, unique = true, length = 7, columnDefinition = "char(7)")
    private YearMonth periodo;

    @Column(name = "fecha_inicio_proceso", nullable = false)
    private LocalDateTime fechaInicioProceso;

    @Column(name = "fecha_fin_proceso")
    private LocalDateTime fechaFinProceso;

    @Convert(converter = EstadoLoteConverter.class)
    @Column(nullable = false, length = 20)
    private EstadoLote estado;

    @Column(name = "total_facturas_generadas", nullable = false)
    private int totalFacturasGeneradas;

    protected LoteFacturacion() {
        // Requerido por JPA
    }

    /** Factory Method: abre un lote nuevo para el periodo. */
    public static LoteFacturacion abrir(YearMonth periodo, LocalDateTime ahora) {
        LoteFacturacion lote = new LoteFacturacion();
        lote.periodo = periodo;
        lote.fechaInicioProceso = ahora;
        lote.estado = EstadoLote.EN_PROCESO;
        lote.totalFacturasGeneradas = 0;
        return lote;
    }

    /**
     * Re-ejecuta un lote existente (tras un error o para facturar contratos que antes no tenían
     * lectura). Las facturas ya emitidas no se duplican: la generación es idempotente.
     */
    public void reabrir(LocalDateTime ahora) {
        if (estado == EstadoLote.EN_PROCESO) {
            throw new ReglaNegocioException(
                    "El lote del periodo %s ya se encuentra en proceso".formatted(periodo));
        }
        this.estado = EstadoLote.EN_PROCESO;
        this.fechaInicioProceso = ahora;
        this.fechaFinProceso = null;
    }

    public void finalizar(int totalFacturas, boolean conErrores, LocalDateTime ahora) {
        this.totalFacturasGeneradas = totalFacturas;
        this.estado = conErrores ? EstadoLote.ERROR : EstadoLote.COMPLETADO;
        this.fechaFinProceso = ahora;
    }

    public void marcarError(LocalDateTime ahora) {
        this.estado = EstadoLote.ERROR;
        this.fechaFinProceso = ahora;
    }

    public boolean estaEnProceso() {
        return estado == EstadoLote.EN_PROCESO;
    }

    public boolean estaCompletado() {
        return estado == EstadoLote.COMPLETADO;
    }

    public Duration duracion() {
        if (fechaInicioProceso == null || fechaFinProceso == null) {
            return null;
        }
        return Duration.between(fechaInicioProceso, fechaFinProceso);
    }

    public Integer getId() {
        return id;
    }

    public YearMonth getPeriodo() {
        return periodo;
    }

    public LocalDateTime getFechaInicioProceso() {
        return fechaInicioProceso;
    }

    public LocalDateTime getFechaFinProceso() {
        return fechaFinProceso;
    }

    public EstadoLote getEstado() {
        return estado;
    }

    public int getTotalFacturasGeneradas() {
        return totalFacturasGeneradas;
    }
}
