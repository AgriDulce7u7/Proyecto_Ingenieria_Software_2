package co.edu.uniquindio.epq.cartera.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import co.edu.uniquindio.epq.comercial.dominio.Contrato;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.pagos.dominio.CanalPago;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Acuerdo de pago en cuotas para un contrato moroso (RF-12). Agregado raíz de sus cuotas.
 */
@Entity
@Table(name = "acuerdo_pago")
public class AcuerdoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_acuerdo", nullable = false, unique = true, length = 30, updatable = false)
    private String numeroAcuerdo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, updatable = false)
    private Contrato contrato;

    @Column(name = "fecha_acuerdo", nullable = false, updatable = false)
    private LocalDateTime fechaAcuerdo;

    @Column(name = "total_deuda", nullable = false, precision = 14, scale = 2, updatable = false)
    private BigDecimal totalDeuda;

    @Column(name = "numero_cuotas", nullable = false, updatable = false)
    private short numeroCuotas;

    @Convert(converter = EstadoAcuerdoConverter.class)
    @Column(nullable = false, length = 20)
    private EstadoAcuerdo estado;

    @Column(name = "fecha_cambio_estado")
    private LocalDateTime fechaCambioEstado;

    @Column(length = 500)
    private String observacion;

    @Column(name = "registrado_por", nullable = false, length = 150, updatable = false)
    private String registradoPor;

    @OneToMany(mappedBy = "acuerdo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroCuota ASC")
    private List<CuotaAcuerdo> cuotas = new ArrayList<>();

    protected AcuerdoPago() {
        // Requerido por JPA
    }

    public static AcuerdoPago registrar(String numeroAcuerdo, Contrato contrato, PlanCuotas plan, String observacion,
                                        String registradoPor, LocalDateTime ahora) {
        if (plan.numeroCuotas() < 1) {
            throw new ReglaNegocioException("El acuerdo debe tener al menos una cuota");
        }
        AcuerdoPago a = new AcuerdoPago();
        a.numeroAcuerdo = Objects.requireNonNull(numeroAcuerdo);
        a.contrato = Objects.requireNonNull(contrato);
        a.totalDeuda = plan.totalDeuda();
        a.numeroCuotas = (short) plan.numeroCuotas();
        a.estado = EstadoAcuerdo.VIGENTE;
        a.observacion = observacion;
        a.registradoPor = registradoPor;
        a.fechaAcuerdo = ahora;
        plan.cuotas().forEach(c -> a.cuotas.add(CuotaAcuerdo.de(a, c)));
        return a;
    }

    /**
     * Registra el pago de una cuota. Las cuotas se pagan en orden y por su valor exacto.
     *
     * @return {@code true} si con este pago el acuerdo quedó cumplido
     */
    public boolean pagarCuota(int numeroCuota, BigDecimal valor, CanalPago canal, String referencia,
                              LocalDateTime fecha) {
        validarVigente();
        CuotaAcuerdo siguiente = siguienteCuotaPendiente();
        if (siguiente.getNumeroCuota() != numeroCuota) {
            throw new ReglaNegocioException("Debe pagarse primero la cuota %d".formatted(siguiente.getNumeroCuota()));
        }
        if (valor != null && valor.compareTo(siguiente.getValor()) != 0) {
            throw new ReglaNegocioException("El valor de la cuota %d es %s".formatted(numeroCuota, siguiente.getValor()));
        }
        siguiente.registrarPago(canal, referencia, fecha);
        if (cuotas.stream().allMatch(c -> c.getEstado() == EstadoCuota.PAGADA)) {
            this.estado = EstadoAcuerdo.CUMPLIDO;
            this.fechaCambioEstado = fecha;
            return true;
        }
        return false;
    }

    /** RN-07 / SWR-31: hay una cuota pendiente cuya fecha de vencimiento ya pasó. */
    public boolean tieneCuotaVencida(LocalDate hoy) {
        return estado == EstadoAcuerdo.VIGENTE && cuotas.stream().anyMatch(c -> c.estaPendienteVencida(hoy));
    }

    /** SWR-31: el acuerdo se invalida automáticamente; las cuotas vencidas quedan marcadas. */
    public void marcarIncumplido(LocalDate hoy, LocalDateTime ahora) {
        validarVigente();
        cuotas.stream().filter(c -> c.estaPendienteVencida(hoy)).forEach(CuotaAcuerdo::marcarVencida);
        this.estado = EstadoAcuerdo.INCUMPLIDO;
        this.fechaCambioEstado = ahora;
    }

    public CuotaAcuerdo siguienteCuotaPendiente() {
        return cuotas.stream()
                .filter(c -> c.getEstado() == EstadoCuota.PENDIENTE)
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException("El acuerdo " + numeroAcuerdo + " no tiene cuotas pendientes"));
    }

    public BigDecimal valorPagado() {
        return cuotas.stream().filter(c -> c.getEstado() == EstadoCuota.PAGADA).map(CuotaAcuerdo::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal saldoPendiente() {
        return totalDeuda.subtract(valorPagado());
    }

    public boolean estaVigente() {
        return estado == EstadoAcuerdo.VIGENTE;
    }

    private void validarVigente() {
        if (estado != EstadoAcuerdo.VIGENTE) {
            throw new ReglaNegocioException("El acuerdo %s está %s".formatted(numeroAcuerdo, estado));
        }
    }

    public Integer getId() {
        return id;
    }

    public String getNumeroAcuerdo() {
        return numeroAcuerdo;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public LocalDateTime getFechaAcuerdo() {
        return fechaAcuerdo;
    }

    public BigDecimal getTotalDeuda() {
        return totalDeuda;
    }

    public int getNumeroCuotas() {
        return numeroCuotas;
    }

    public EstadoAcuerdo getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCambioEstado() {
        return fechaCambioEstado;
    }

    public String getObservacion() {
        return observacion;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public List<CuotaAcuerdo> getCuotas() {
        return Collections.unmodifiableList(cuotas);
    }
}
