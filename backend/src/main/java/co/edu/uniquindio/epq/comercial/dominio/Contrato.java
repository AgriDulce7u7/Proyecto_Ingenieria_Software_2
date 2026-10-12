package co.edu.uniquindio.epq.comercial.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.excepcion.TransicionEstadoInvalidaException;
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
 * Contrato de servicio entre un usuario y EPQ para un predio (F-04).
 * Entidad rica: controla su ciclo de vida (State en {@link EstadoContrato}) y la regla SWR-19.
 */
@Entity
@Table(name = "contrato")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_contrato", nullable = false, unique = true, length = 30, updatable = false)
    private String numeroContrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predio_id")
    private Predio predio;

    /** Tarifa aplicable asignada al registrar el contrato (RF-04, SWR-18). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifa_id")
    private Tarifa tarifa;

    @Column(name = "direccion_servicio", nullable = false, length = 200)
    private String direccionServicio;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Convert(converter = EstadoContratoConverter.class)
    @Column(nullable = false, length = 20)
    private EstadoContrato estado;

    /** SWR-32: dos o más períodos consecutivos de mora sin acuerdo de pago vigente. */
    @Column(name = "candidato_suspension", nullable = false)
    private boolean candidatoSuspension;

    @Column(name = "fecha_candidato_suspension")
    private LocalDateTime fechaCandidatoSuspension;

    @Column(name = "registrado_por", length = 150, updatable = false)
    private String registradoPor;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    protected Contrato() {
        // Requerido por JPA
    }

    /** Factory Method: el contrato nace PENDIENTE y se activa en un paso explícito. */
    public static Contrato registrar(String numeroContrato, Cliente cliente, Predio predio, Servicio servicio,
                                     Tarifa tarifa, LocalDate fechaInicio, String registradoPor, LocalDateTime ahora) {
        if (!cliente.isActivo()) {
            throw new ReglaNegocioException("El usuario " + cliente.getCodigoInterno() + " está inactivo");
        }
        Contrato c = new Contrato();
        c.numeroContrato = Objects.requireNonNull(numeroContrato);
        c.cliente = cliente;
        c.predio = Objects.requireNonNull(predio);
        c.servicio = Objects.requireNonNull(servicio);
        c.tarifa = tarifa;
        c.direccionServicio = predio.getDireccion();
        c.fechaInicio = fechaInicio;
        c.estado = EstadoContrato.PENDIENTE;
        c.registradoPor = registradoPor;
        c.fechaRegistro = ahora;
        return c;
    }

    /**
     * Activa (o reactiva) el contrato.
     *
     * @param tieneMedidorActivo si el contrato tiene un medidor instalado (SWR-19)
     */
    public void activar(boolean tieneMedidorActivo) {
        if (tipoServicio().requiereMedidor() && !tieneMedidorActivo) {
            throw new ReglaNegocioException("El contrato %s de %s requiere número de medidor antes de activarse (SWR-19)"
                    .formatted(numeroContrato, servicio.getNombre()));
        }
        transitarA(EstadoContrato.ACTIVO);
        this.candidatoSuspension = false;
        this.fechaCandidatoSuspension = null;
    }

    public void suspender() {
        transitarA(EstadoContrato.SUSPENDIDO);
        this.candidatoSuspension = false;
        this.fechaCandidatoSuspension = null;
    }

    public void cancelar() {
        transitarA(EstadoContrato.CANCELADO);
        this.candidatoSuspension = false;
        this.fechaCandidatoSuspension = null;
    }

    private void transitarA(EstadoContrato destino) {
        if (!estado.puedeTransitarA(destino)) {
            throw new TransicionEstadoInvalidaException(estado.name(), destino.name());
        }
        this.estado = destino;
    }

    /** SWR-32: marca o desmarca el contrato como candidato a suspensión. Retorna true si cambió. */
    public boolean actualizarCandidatoSuspension(boolean esCandidato, LocalDateTime ahora) {
        if (esCandidato == candidatoSuspension) {
            return false;
        }
        this.candidatoSuspension = esCandidato;
        this.fechaCandidatoSuspension = esCandidato ? ahora : null;
        return true;
    }

    public void asignarTarifa(Tarifa nuevaTarifa) {
        this.tarifa = nuevaTarifa;
    }

    /** RF-05 / EST-07: solo los contratos activos generan ciclo de facturación. */
    public boolean esFacturable() {
        return estado == EstadoContrato.ACTIVO;
    }

    public TipoServicio tipoServicio() {
        return TipoServicio.desdeNombre(servicio.getNombre());
    }

    public Integer getId() {
        return id;
    }

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public Predio getPredio() {
        return predio;
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    public String getDireccionServicio() {
        return direccionServicio;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public EstadoContrato getEstado() {
        return estado;
    }

    public boolean isCandidatoSuspension() {
        return candidatoSuspension;
    }

    public LocalDateTime getFechaCandidatoSuspension() {
        return fechaCandidatoSuspension;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
