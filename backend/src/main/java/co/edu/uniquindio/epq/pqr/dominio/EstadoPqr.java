package co.edu.uniquindio.epq.pqr.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "estado_pqr")
public class EstadoPqr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    @Column(nullable = false)
    private Short orden;

    protected EstadoPqr() {
        // Requerido por JPA
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Short getOrden() {
        return orden;
    }

    public EstadoPqrTipo getTipo() {
        return EstadoPqrTipo.desdeNombre(nombre);
    }
}
