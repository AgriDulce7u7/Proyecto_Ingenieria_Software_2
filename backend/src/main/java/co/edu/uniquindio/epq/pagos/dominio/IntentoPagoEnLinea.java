package co.edu.uniquindio.epq.pagos.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Trazabilidad de cada intento de pago en línea (aprobado, rechazado o sin respuesta).
 * Nunca guarda el número de tarjeta completo: solo los últimos cuatro dígitos.
 */
@Entity
@Table(name = "intento_pago_en_linea")
public class IntentoPagoEnLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "factura_id", nullable = false)
    private Long facturaId;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Convert(converter = ResultadoIntentoPagoConverter.class)
    @Column(nullable = false, length = 20)
    private ResultadoIntentoPago resultado;

    @Column(name = "codigo_transaccion", length = 60)
    private String codigoTransaccion;

    @Column(name = "codigo_respuesta", length = 30)
    private String codigoRespuesta;

    @Column(length = 300)
    private String mensaje;

    @Column(name = "ultimos_digitos", length = 4)
    private String ultimosDigitos;

    @Column(nullable = false)
    private short intentos;

    @Column(nullable = false, length = 150)
    private String usuario;

    protected IntentoPagoEnLinea() {
        // Requerido por JPA
    }

    public static IntentoPagoEnLinea registrar(Long facturaId, BigDecimal valor, ResultadoIntentoPago resultado,
                                               String codigoTransaccion, String codigoRespuesta, String mensaje,
                                               String ultimosDigitos, int intentos, String usuario, LocalDateTime fecha) {
        IntentoPagoEnLinea i = new IntentoPagoEnLinea();
        i.facturaId = facturaId;
        i.valor = valor;
        i.resultado = resultado;
        i.codigoTransaccion = codigoTransaccion;
        i.codigoRespuesta = codigoRespuesta;
        i.mensaje = mensaje != null && mensaje.length() > 300 ? mensaje.substring(0, 300) : mensaje;
        i.ultimosDigitos = ultimosDigitos;
        i.intentos = (short) intentos;
        i.usuario = usuario;
        i.fecha = fecha;
        return i;
    }

    public Long getId() {
        return id;
    }

    public Long getFacturaId() {
        return facturaId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public ResultadoIntentoPago getResultado() {
        return resultado;
    }

    public String getCodigoTransaccion() {
        return codigoTransaccion;
    }

    public String getCodigoRespuesta() {
        return codigoRespuesta;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getUltimosDigitos() {
        return ultimosDigitos;
    }

    public short getIntentos() {
        return intentos;
    }

    public String getUsuario() {
        return usuario;
    }
}
