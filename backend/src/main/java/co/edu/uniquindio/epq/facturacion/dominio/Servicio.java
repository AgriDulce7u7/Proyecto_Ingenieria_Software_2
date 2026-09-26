package co.edu.uniquindio.epq.facturacion.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Servicio público domiciliario (Acueducto, Alcantarillado, Gas).
 */
@Entity
@Table(name = "servicio")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    protected Servicio() {
        // Requerido por JPA
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
