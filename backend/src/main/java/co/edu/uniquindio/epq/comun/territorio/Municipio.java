package co.edu.uniquindio.epq.comun.territorio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Municipio atendido por EPQ (CO-03). Catálogo compartido por los módulos comercial, PQR y cartera.
 */
@Entity
@Table(name = "municipio")
public class Municipio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(name = "codigo_dane", nullable = false, unique = true, length = 10)
    private String codigoDane;

    @Column(nullable = false)
    private boolean activo;

    protected Municipio() {
        // Requerido por JPA
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigoDane() {
        return codigoDane;
    }

    public boolean isActivo() {
        return activo;
    }
}
