package co.edu.uniquindio.epq.comercial.dominio;

import java.time.LocalDateTime;
import java.util.Objects;

import co.edu.uniquindio.epq.comun.territorio.Municipio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Inmueble donde se presta el servicio. Su municipio y estrato determinan la tarifa (RN-08).
 */
@Entity
@Table(name = "predio")
public class Predio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo_predio", nullable = false, unique = true, length = 20, updatable = false)
    private String codigoPredio;

    @Column(nullable = false, length = 200)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "municipio_id", nullable = false)
    private Municipio municipio;

    @Column(nullable = false)
    private short estrato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private UsoPredio uso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    protected Predio() {
        // Requerido por JPA
    }

    public static Predio registrar(String codigoPredio, String direccion, Municipio municipio, int estrato,
                                   UsoPredio uso, Cliente cliente, LocalDateTime ahora) {
        if (estrato < 1 || estrato > 6) {
            throw new IllegalArgumentException("El estrato debe estar entre 1 y 6");
        }
        Predio p = new Predio();
        p.codigoPredio = Objects.requireNonNull(codigoPredio);
        p.direccion = Objects.requireNonNull(direccion).trim();
        p.municipio = Objects.requireNonNull(municipio);
        p.estrato = (short) estrato;
        p.uso = uso == null ? UsoPredio.RESIDENCIAL : uso;
        p.cliente = cliente;
        p.fechaRegistro = ahora;
        return p;
    }

    public Integer getId() {
        return id;
    }

    public String getCodigoPredio() {
        return codigoPredio;
    }

    public String getDireccion() {
        return direccion;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public int getEstrato() {
        return estrato;
    }

    public UsoPredio getUso() {
        return uso;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
