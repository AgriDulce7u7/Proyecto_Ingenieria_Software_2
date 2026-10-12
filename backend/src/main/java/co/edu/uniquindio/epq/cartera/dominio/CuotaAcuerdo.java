package co.edu.uniquindio.epq.cartera.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.pagos.dominio.CanalPago;
import co.edu.uniquindio.epq.pagos.dominio.CanalPagoConverter;
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
 * Cuota individual de un acuerdo de pago (modelo de dominio: CuotaAcuerdo).
 */
@Entity
@Table(name = "cuota_acuerdo")
public class CuotaAcuerdo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acuerdo_id", nullable = false)
    private AcuerdoPago acuerdo;

    @Column(name = "numero_cuota", nullable = false)
    private short numeroCuota;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Convert(converter = EstadoCuotaConverter.class)
    @Column(nullable = false, length = 20)
    private EstadoCuota estado;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Convert(converter = CanalPagoConverter.class)
    @Column(name = "canal_pago", length = 20)
    private CanalPago canalPago;

    @Column(name = "referencia_pago", length = 100)
    private String referenciaPago;

    protected CuotaAcuerdo() {
        // Requerido por JPA
    }

    static CuotaAcuerdo de(AcuerdoPago acuerdo, CuotaPlan plan) {
        CuotaAcuerdo c = new CuotaAcuerdo();
        c.acuerdo = acuerdo;
        c.numeroCuota = (short) plan.numero();
        c.valor = plan.valor();
        c.fechaVencimiento = plan.fechaVencimiento();
        c.estado = EstadoCuota.PENDIENTE;
        return c;
    }

    void registrarPago(CanalPago canal, String referencia, LocalDateTime fecha) {
        if (estado != EstadoCuota.PENDIENTE) {
            throw new ReglaNegocioException("La cuota %d está en estado %s".formatted(numeroCuota, estado));
        }
        this.estado = EstadoCuota.PAGADA;
        this.canalPago = canal;
        this.referenciaPago = referencia;
        this.fechaPago = fecha;
    }

    void marcarVencida() {
        if (estado == EstadoCuota.PENDIENTE) {
            this.estado = EstadoCuota.VENCIDA;
        }
    }

    public boolean estaPendienteVencida(LocalDate hoy) {
        return estado == EstadoCuota.PENDIENTE && fechaVencimiento.isBefore(hoy);
    }

    public Integer getId() {
        return id;
    }

    public AcuerdoPago getAcuerdo() {
        return acuerdo;
    }

    public int getNumeroCuota() {
        return numeroCuota;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public EstadoCuota getEstado() {
        return estado;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public CanalPago getCanalPago() {
        return canalPago;
    }

    public String getReferenciaPago() {
        return referenciaPago;
    }
}
