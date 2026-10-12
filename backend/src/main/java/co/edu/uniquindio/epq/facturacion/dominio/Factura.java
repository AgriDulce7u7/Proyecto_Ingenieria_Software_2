package co.edu.uniquindio.epq.facturacion.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import co.edu.uniquindio.epq.comercial.dominio.Contrato;
import co.edu.uniquindio.epq.comercial.dominio.Tarifa;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.excepcion.TransicionEstadoInvalidaException;
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

    // ------------------------------------------------------------------ ciclo de cobro (DE-01, entrega 2)

    @Convert(converter = EstadoCobroFacturaConverter.class)
    @Column(name = "estado_cobro", nullable = false, length = 20)
    private EstadoCobroFactura estadoCobro = EstadoCobroFactura.PENDIENTE;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "motivo_anulacion", length = 500)
    private String motivoAnulacion;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @Column(name = "anulada_por", length = 150)
    private String anuladaPor;

    /** Acuerdo de pago vigente que cubre esta factura (RF-12); nulo si no está en un acuerdo. */
    @Column(name = "acuerdo_pago_id")
    private Integer acuerdoPagoId;

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
        factura.estadoCobro = EstadoCobroFactura.PENDIENTE;
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

    // ------------------------------------------------------------------ comportamiento de cobro (DE-01)

    /** SWR-23 / RF-06: una factura pagada o anulada no admite pagos; tampoco una incluida en un acuerdo vigente. */
    public void validarPagable() {
        if (estadoCobro == EstadoCobroFactura.PAGADA) {
            throw new ReglaNegocioException("La factura %s ya se encuentra pagada".formatted(numeroFactura));
        }
        if (estadoCobro == EstadoCobroFactura.ANULADA) {
            throw new ReglaNegocioException("La factura %s está anulada y no admite pagos".formatted(numeroFactura));
        }
        if (acuerdoPagoId != null) {
            throw new ReglaNegocioException(("La factura %s está incluida en un acuerdo de pago vigente: "
                    + "el pago se realiza mediante las cuotas del acuerdo").formatted(numeroFactura));
        }
    }

    /** RN-05: solo se llama cuando hay confirmación del pago (funcionario o pasarela). */
    public void registrarPago(LocalDateTime fecha) {
        validarPagable();
        cambiarEstadoCobro(EstadoCobroFactura.PAGADA);
        this.fechaPago = fecha;
    }

    /** DE-01 vencerPlazo(): la fecha de vencimiento pasó sin pago. Retorna true si cambió. */
    public boolean vencerSiCorresponde(LocalDate hoy) {
        if (estadoCobro == EstadoCobroFactura.PENDIENTE && acuerdoPagoId == null && fechaVencimiento.isBefore(hoy)) {
            cambiarEstadoCobro(EstadoCobroFactura.VENCIDA);
            return true;
        }
        return false;
    }

    public void anular(String motivo, String usuario, LocalDateTime fecha) {
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioException("La anulación de una factura requiere un motivo");
        }
        if (acuerdoPagoId != null) {
            throw new ReglaNegocioException("La factura %s está incluida en un acuerdo de pago vigente".formatted(numeroFactura));
        }
        cambiarEstadoCobro(EstadoCobroFactura.ANULADA);
        this.motivoAnulacion = motivo.trim();
        this.anuladaPor = usuario;
        this.fechaAnulacion = fecha;
    }

    /** DE-01 generarAcuerdoPago(): la factura vencida queda PENDIENTE cubierta por el acuerdo. */
    public void incluirEnAcuerdo(Integer acuerdoId) {
        if (estadoCobro != EstadoCobroFactura.VENCIDA || acuerdoPagoId != null) {
            throw new ReglaNegocioException("Solo las facturas vencidas sin acuerdo pueden incluirse en un acuerdo de pago");
        }
        cambiarEstadoCobro(EstadoCobroFactura.PENDIENTE);
        this.acuerdoPagoId = acuerdoId;
    }

    /** RN-07: al incumplirse el acuerdo la factura sale de él y vuelve a estar vencida. */
    public void liberarDeAcuerdo() {
        if (acuerdoPagoId == null) {
            return;
        }
        this.acuerdoPagoId = null;
        if (estadoCobro == EstadoCobroFactura.PENDIENTE) {
            cambiarEstadoCobro(EstadoCobroFactura.VENCIDA);
        }
    }

    /** El acuerdo se cumplió: las facturas que cubría quedan pagadas. */
    public void marcarPagadaPorAcuerdo(LocalDateTime fecha) {
        if (acuerdoPagoId == null) {
            throw new ReglaNegocioException("La factura %s no pertenece a un acuerdo".formatted(numeroFactura));
        }
        cambiarEstadoCobro(EstadoCobroFactura.PAGADA);
        this.fechaPago = fecha;
    }

    public boolean estaEnMora() {
        return estadoCobro == EstadoCobroFactura.VENCIDA && acuerdoPagoId == null;
    }

    private void cambiarEstadoCobro(EstadoCobroFactura destino) {
        EstadoCobroFactura actual = estadoCobro == null ? EstadoCobroFactura.PENDIENTE : estadoCobro;
        if (!actual.puedeTransitarA(destino)) {
            throw new TransicionEstadoInvalidaException(actual.name(), destino.name());
        }
        this.estadoCobro = destino;
    }

    public EstadoCobroFactura getEstadoCobro() {
        return estadoCobro;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public LocalDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }

    public String getAnuladaPor() {
        return anuladaPor;
    }

    public Integer getAcuerdoPagoId() {
        return acuerdoPagoId;
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
