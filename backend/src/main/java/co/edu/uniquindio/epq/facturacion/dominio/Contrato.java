package co.edu.uniquindio.epq.facturacion.dominio;

import java.time.LocalDate;

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
 * Contrato de servicio entre un cliente y EPQ para un predio (dirección de servicio).
 */
@Entity
@Table(name = "contrato")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_contrato", nullable = false, unique = true, length = 30)
    private String numeroContrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Column(name = "direccion_servicio", nullable = false, length = 200)
    private String direccionServicio;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Convert(converter = EstadoContratoConverter.class)
    @Column(nullable = false, length = 20)
    private EstadoContrato estado;

    protected Contrato() {
        // Requerido por JPA
    }

    /** RF-05 / EST-07: solo los contratos activos generan ciclo de facturación. */
    public boolean esFacturable() {
        return estado == EstadoContrato.ACTIVO;
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

    public String getDireccionServicio() {
        return direccionServicio;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public EstadoContrato getEstado() {
        return estado;
    }
}
