package co.edu.uniquindio.epq.pqr.dominio;

import java.time.LocalDateTime;

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
 * Notificación enviada al ciudadano o al gestor en relación con una PQR.
 * Si {@code gestor} es nulo, la notificación fue dirigida al ciudadano.
 */
@Entity
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pqr_id", nullable = false)
    private Pqr pqr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestor_id")
    private Gestor gestor;

    @Convert(converter = TipoNotificacionConverter.class)
    @Column(nullable = false, length = 50)
    private TipoNotificacion tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "enviado_ok", nullable = false)
    private boolean enviadoOk;

    protected Notificacion() {
        // Requerido por JPA
    }

    public static Notificacion registrar(Pqr pqr, Gestor gestor, TipoNotificacion tipo, String mensaje,
                                         LocalDateTime fechaEnvio, boolean enviadoOk) {
        Notificacion notificacion = new Notificacion();
        notificacion.pqr = pqr;
        notificacion.gestor = gestor;
        notificacion.tipo = tipo;
        notificacion.mensaje = mensaje;
        notificacion.fechaEnvio = fechaEnvio;
        notificacion.enviadoOk = enviadoOk;
        return notificacion;
    }

    public Long getId() {
        return id;
    }

    public Pqr getPqr() {
        return pqr;
    }

    public Gestor getGestor() {
        return gestor;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public boolean isEnviadoOk() {
        return enviadoOk;
    }
}
