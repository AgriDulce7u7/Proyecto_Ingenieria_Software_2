package co.edu.uniquindio.epq.comun.notificacion;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Notificación enviada a un funcionario o a un usuario del servicio (IS-SIS-02): comprobantes de pago,
 * alertas de cartera, incumplimientos de acuerdos, etc. Las notificaciones de PQR usan su propia tabla.
 */
@Entity
@Table(name = "notificacion_sistema")
public class NotificacionSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String destinatario;

    @Column(name = "funcionario_id")
    private Integer funcionarioId;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, length = 200)
    private String asunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(length = 60)
    private String referencia;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "enviado_ok", nullable = false)
    private boolean enviadoOk;

    @Column(nullable = false)
    private boolean leida;

    protected NotificacionSistema() {
        // Requerido por JPA
    }

    public static NotificacionSistema registrar(String destinatario, Integer funcionarioId, String tipo, String asunto,
                                                String mensaje, String referencia, LocalDateTime fecha, boolean enviadoOk) {
        NotificacionSistema n = new NotificacionSistema();
        n.destinatario = destinatario;
        n.funcionarioId = funcionarioId;
        n.tipo = tipo;
        n.asunto = asunto;
        n.mensaje = mensaje;
        n.referencia = referencia;
        n.fechaEnvio = fecha;
        n.enviadoOk = enviadoOk;
        n.leida = false;
        return n;
    }

    public void marcarLeida() {
        this.leida = true;
    }

    public Long getId() {
        return id;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public Integer getFuncionarioId() {
        return funcionarioId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getReferencia() {
        return referencia;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public boolean isEnviadoOk() {
        return enviadoOk;
    }

    public boolean isLeida() {
        return leida;
    }
}
