package co.edu.uniquindio.epq.facturacion.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
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
 * Factura mensual de un contrato. Entidad rica: controla sus propias transiciones de estado.
 */
@Entity
@Table(name = "factura")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_factura", nullable = false, unique = true, length = 30)
    private String numeroFactura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private LoteFacturacion lote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lectura_id", nullable = false)
    private LecturaMedidor lectura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tarifa_id", nullable = false)
    private Tarifa tarifa;

    @Convert(converter = PeriodoConverter.class)
    @Column(nullable = false, length = 7, columnDefinition = "char(7)")
    private YearMonth periodo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal consumo;

    @Column(name = "valor_consumo", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorConsumo;

    @Column(name = "cargo_fijo", nullable = false, precision = 12, scale = 2)
    private BigDecimal cargoFijo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Convert(converter = EstadoFacturaConverter.class)
    @Column(nullable = false, length = 20)
    private EstadoFactura estado;

    protected Factura() {
        // Requerido por JPA
    }

    /**
     * Factory Method: emite una factura a partir de la liquidación calculada.
     */
    public static Factura emitir(String numeroFactura, LoteFacturacion lote, Contrato contrato,
                                 LecturaMedidor lectura, Tarifa tarifa, DetalleLiquidacion liquidacion,
                                 LocalDateTime fechaGeneracion, LocalDate fechaVencimiento) {
        if (!contrato.esFacturable()) {
            throw new ReglaNegocioException(
                    "El contrato %s no está activo".formatted(contrato.getNumeroContrato()));
        }
        Factura factura = new Factura();
        factura.numeroFactura = numeroFactura;
        factura.lote = lote;
        factura.contrato = contrato;
        factura.lectura = lectura;
        factura.tarifa = tarifa;
        factura.periodo = lote.getPeriodo();
        factura.consumo = liquidacion.consumo();
        factura.valorConsumo = liquidacion.valorConsumo();
        factura.cargoFijo = liquidacion.cargoFijo();
        factura.subtotal = liquidacion.subtotal();
        factura.total = liquidacion.total();
        factura.fechaGeneracion = fechaGeneracion;
        factura.fechaVencimiento = fechaVencimiento;
        factura.estado = EstadoFactura.GENERADA;
        return factura;
    }

    /** SWR-04: solo facturas generadas o con error previo pueden enviarse al ERP. */
    public boolean requiereSincronizacion() {
        return estado == EstadoFactura.GENERADA || estado == EstadoFactura.ERROR_SINCRONIZACION;
    }

    public void marcarSincronizada() {
        validarSincronizable();
        this.estado = EstadoFactura.SINCRONIZADA;
    }

    public void marcarErrorSincronizacion() {
        validarSincronizable();
        this.estado = EstadoFactura.ERROR_SINCRONIZACION;
    }

    private void validarSincronizable() {
        if (!requiereSincronizacion()) {
            throw new ReglaNegocioException("La factura %s está en estado '%s' y no requiere sincronización"
                    .formatted(numeroFactura, estado.codigo()));
        }
    }

    public Long getId() {
        return id;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public LoteFacturacion getLote() {
        return lote;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public LecturaMedidor getLectura() {
        return lectura;
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    public YearMonth getPeriodo() {
        return periodo;
    }

    public BigDecimal getConsumo() {
        return consumo;
    }

    public BigDecimal getValorConsumo() {
        return valorConsumo;
    }

    public BigDecimal getCargoFijo() {
        return cargoFijo;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public EstadoFactura getEstado() {
        return estado;
    }
}
