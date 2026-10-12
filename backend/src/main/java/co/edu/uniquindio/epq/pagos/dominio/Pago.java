package co.edu.uniquindio.epq.pagos.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import co.edu.uniquindio.epq.facturacion.dominio.Factura;
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
 * Pago confirmado de una factura (F-05). Inmutable: solo se crea cuando hay confirmación (RN-05).
 */
@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_pago", nullable = false, unique = true, length = 30, updatable = false)
    private String codigoPago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id", nullable = false, unique = true, updatable = false)
    private Factura factura;

    @Column(nullable = false, precision = 12, scale = 2, updatable = false)
    private BigDecimal valor;

    @Convert(converter = CanalPagoConverter.class)
    @Column(nullable = false, length = 20, updatable = false)
    private CanalPago canal;

    @Column(name = "fecha_pago", nullable = false, updatable = false)
    private LocalDateTime fechaPago;

    @Column(name = "codigo_transaccion", length = 60, updatable = false)
    private String codigoTransaccion;

    @Column(length = 100, updatable = false)
    private String referencia;

    @Column(name = "numero_comprobante", nullable = false, unique = true, length = 30, updatable = false)
    private String numeroComprobante;

    @Column(name = "registrado_por", nullable = false, length = 150, updatable = false)
    private String registradoPor;

    protected Pago() {
        // Requerido por JPA
    }

    public static Pago registrar(String codigoPago, Factura factura, BigDecimal valor, CanalPago canal,
                                 LocalDateTime fecha, String codigoTransaccion, String referencia,
                                 String numeroComprobante, String registradoPor) {
        if (canal == CanalPago.EN_LINEA && (codigoTransaccion == null || codigoTransaccion.isBlank())) {
            throw new IllegalArgumentException("Un pago en línea requiere el código de transacción de la pasarela (RN-05)");
        }
        Pago p = new Pago();
        p.codigoPago = Objects.requireNonNull(codigoPago);
        p.factura = Objects.requireNonNull(factura);
        p.valor = Objects.requireNonNull(valor);
        p.canal = Objects.requireNonNull(canal);
        p.fechaPago = Objects.requireNonNull(fecha);
        p.codigoTransaccion = codigoTransaccion;
        p.referencia = referencia;
        p.numeroComprobante = Objects.requireNonNull(numeroComprobante);
        p.registradoPor = Objects.requireNonNull(registradoPor);
        return p;
    }

    public Long getId() {
        return id;
    }

    public String getCodigoPago() {
        return codigoPago;
    }

    public Factura getFactura() {
        return factura;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public CanalPago getCanal() {
        return canal;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public String getCodigoTransaccion() {
        return codigoTransaccion;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }
}
