package co.edu.uniquindio.epq.pqr.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.excepcion.TransicionEstadoInvalidaException;
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
 * Agregado raíz de una Petición, Queja o Reclamo.
 *
 * <p>Modelo de dominio rico: las reglas de negocio del ciclo de vida (DE-02, RN-04, RN-06) viven
 * en la entidad y no en los servicios. No expone setters públicos; el estado solo cambia mediante
 * operaciones con significado de negocio.</p>
 */
@Entity
@Table(name = "pqr")
public class Pqr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30, updatable = false)
    private String radicado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ciudadano_id", nullable = false)
    private Ciudadano ciudadano;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_solicitud_id", nullable = false)
    private TipoSolicitud tipoSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canal_id")
    private CanalAtencion canal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoPqr estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestor_id")
    private Gestor gestor;

    @Column(nullable = false, length = 200)
    private String asunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_recepcion", nullable = false, updatable = false)
    private LocalDateTime fechaRecepcion;

    @Column(name = "fecha_limite_respuesta", nullable = false)
    private LocalDateTime fechaLimiteRespuesta;

    @Column(name = "fecha_estimada_respuesta", nullable = false)
    private LocalDate fechaEstimadaRespuesta;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(columnDefinition = "TEXT")
    private String respuesta;

    @Column(name = "notificado_vencimiento", nullable = false)
    private boolean notificadoVencimiento;

    protected Pqr() {
        // Requerido por JPA
    }

    /**
     * Crea una PQR en estado Radicado (EST-03) con su radicado y plazo normativo calculados.
     */
    public static Pqr radicar(String radicado, Ciudadano ciudadano, TipoSolicitud tipoSolicitud,
                              CanalAtencion canal, EstadoPqr estadoInicial, String asunto,
                              String descripcion, LocalDateTime fechaRecepcion, PlazoRespuesta plazo) {
        if (estadoInicial.getTipo() != EstadoPqrTipo.RADICADO) {
            throw new IllegalArgumentException("Una PQR nueva debe iniciar en estado Radicado");
        }
        Pqr pqr = new Pqr();
        pqr.radicado = Objects.requireNonNull(radicado, "El radicado es obligatorio");
        pqr.ciudadano = Objects.requireNonNull(ciudadano, "El ciudadano es obligatorio");
        pqr.tipoSolicitud = Objects.requireNonNull(tipoSolicitud, "El tipo de solicitud es obligatorio");
        pqr.canal = canal;
        pqr.estado = estadoInicial;
        pqr.asunto = Objects.requireNonNull(asunto, "El asunto es obligatorio");
        pqr.descripcion = Objects.requireNonNull(descripcion, "La descripción es obligatoria");
        pqr.fechaRecepcion = Objects.requireNonNull(fechaRecepcion, "La fecha de recepción es obligatoria");
        pqr.fechaLimiteRespuesta = plazo.fechaLimite();
        pqr.fechaEstimadaRespuesta = plazo.fechaEstimada();
        pqr.notificadoVencimiento = false;
        return pqr;
    }

    /** Asigna (o reasigna) la PQR a un gestor activo mientras siga pendiente de respuesta. */
    public void asignarA(Gestor nuevoGestor) {
        Objects.requireNonNull(nuevoGestor, "El gestor es obligatorio");
        if (!nuevoGestor.isActivo()) {
            throw new ReglaNegocioException("El gestor '%s' está inactivo".formatted(nuevoGestor.getNombre()));
        }
        if (!getEstadoTipo().estaPendienteDeRespuesta()) {
            throw new ReglaNegocioException(
                    "No se puede asignar una PQR en estado '%s'".formatted(estado.getNombre()));
        }
        this.gestor = nuevoGestor;
    }

    /** El gestor toma la PQR para atención (EST-03 → EST-04). */
    public void iniciarTramite(EstadoPqr enTramite, Gestor gestorResponsable) {
        Objects.requireNonNull(gestorResponsable, "El gestor es obligatorio");
        if (!gestorResponsable.isActivo()) {
            throw new ReglaNegocioException(
                    "El gestor '%s' está inactivo".formatted(gestorResponsable.getNombre()));
        }
        cambiarEstado(enTramite);
        this.gestor = gestorResponsable;
    }

    /** Registra la respuesta formal (EST-04 → EST-05). RN-06: la respuesta es obligatoria. */
    public void registrarRespuesta(String textoRespuesta, EstadoPqr resuelto, LocalDateTime fecha) {
        if (textoRespuesta == null || textoRespuesta.isBlank()) {
            throw new ReglaNegocioException("La PQR solo puede resolverse con una respuesta formal registrada (RN-06)");
        }
        if (!getEstadoTipo().admiteRespuesta()) {
            throw new TransicionEstadoInvalidaException(estado.getNombre(), resuelto.getNombre());
        }
        cambiarEstado(resuelto);
        this.respuesta = textoRespuesta.trim();
        this.fechaResolucion = fecha;
    }

    public void cerrar(EstadoPqr cerrado) {
        cambiarEstado(cerrado);
    }

    /** El plazo normativo transcurrió sin respuesta (RN-04). */
    public void marcarVencida(EstadoPqr vencido) {
        cambiarEstado(vencido);
    }

    public void marcarAlertaVencimientoEnviada() {
        this.notificadoVencimiento = true;
    }

    public boolean esGestionadaPor(Gestor candidato) {
        return gestor != null && candidato != null && Objects.equals(gestor.getId(), candidato.getId());
    }

    private void cambiarEstado(EstadoPqr nuevoEstado) {
        Objects.requireNonNull(nuevoEstado, "El nuevo estado es obligatorio");
        EstadoPqrTipo actual = getEstadoTipo();
        EstadoPqrTipo destino = nuevoEstado.getTipo();
        if (!actual.puedeTransitarA(destino)) {
            throw new TransicionEstadoInvalidaException(actual.getNombre(), destino.getNombre());
        }
        this.estado = nuevoEstado;
    }

    public EstadoPqrTipo getEstadoTipo() {
        return estado.getTipo();
    }

    public Long getId() {
        return id;
    }

    public String getRadicado() {
        return radicado;
    }

    public Ciudadano getCiudadano() {
        return ciudadano;
    }

    public TipoSolicitud getTipoSolicitud() {
        return tipoSolicitud;
    }

    public CanalAtencion getCanal() {
        return canal;
    }

    public EstadoPqr getEstado() {
        return estado;
    }

    public Gestor getGestor() {
        return gestor;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechaRecepcion() {
        return fechaRecepcion;
    }

    public LocalDateTime getFechaLimiteRespuesta() {
        return fechaLimiteRespuesta;
    }

    public LocalDate getFechaEstimadaRespuesta() {
        return fechaEstimadaRespuesta;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public boolean isNotificadoVencimiento() {
        return notificadoVencimiento;
    }
}
