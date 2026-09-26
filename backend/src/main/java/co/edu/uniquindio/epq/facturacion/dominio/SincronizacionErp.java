package co.edu.uniquindio.epq.facturacion.dominio;

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
 * Bitácora de cada intento de sincronización de una factura con el ERP financiero (SWR-04, IS-01).
 */
@Entity
@Table(name = "sincronizacion_erp")
public class SincronizacionErp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @Column(name = "fecha_sincronizacion", nullable = false)
    private LocalDateTime fechaSincronizacion;

    @Convert(converter = EstadoSincronizacionConverter.class)
    @Column(nullable = false, length = 20)
    private EstadoSincronizacion estado;

    @Column(name = "respuesta_erp", columnDefinition = "text")
    private String respuestaErp;

    @Column(nullable = false)
    private short intentos;

    protected SincronizacionErp() {
        // Requerido por JPA
    }

    public static SincronizacionErp registrar(Factura factura, EstadoSincronizacion estado, String respuestaErp,
                                              int intentos, LocalDateTime fecha) {
        SincronizacionErp registro = new SincronizacionErp();
        registro.factura = factura;
        registro.estado = estado;
        registro.respuestaErp = respuestaErp;
        registro.intentos = (short) intentos;
        registro.fechaSincronizacion = fecha;
        return registro;
    }

    public Long getId() {
        return id;
    }

    public Factura getFactura() {
        return factura;
    }

    public LocalDateTime getFechaSincronizacion() {
        return fechaSincronizacion;
    }

    public EstadoSincronizacion getEstado() {
        return estado;
    }

    public String getRespuestaErp() {
        return respuestaErp;
    }

    public short getIntentos() {
        return intentos;
    }
}
