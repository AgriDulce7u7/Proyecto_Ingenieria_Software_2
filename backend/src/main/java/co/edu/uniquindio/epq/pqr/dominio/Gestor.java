package co.edu.uniquindio.epq.pqr.dominio;

import co.edu.uniquindio.epq.comun.territorio.Municipio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "gestor")
public class Gestor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(length = 100)
    private String area;

    @Column(nullable = false)
    private boolean activo;

    /** Municipio que atiende (SyR-02b: la PQR se asigna al gestor del municipio). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;

    /** Jefe de la Oficina de PQR (StR-02c). */
    @Column(name = "es_jefe", nullable = false)
    private boolean esJefe;

    /** Jefe inmediato que recibe las alertas de vencimiento del equipo (SWR-11). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jefe_id")
    private Gestor jefe;

    protected Gestor() {
        // Requerido por JPA
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getArea() {
        return area;
    }

    public boolean isActivo() {
        return activo;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public boolean isEsJefe() {
        return esJefe;
    }

    public Gestor getJefe() {
        return jefe;
    }
}
