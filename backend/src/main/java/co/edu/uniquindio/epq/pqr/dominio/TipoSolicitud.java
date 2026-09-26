package co.edu.uniquindio.epq.pqr.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipo_solicitud")
public class TipoSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    protected TipoSolicitud() {
        // Requerido por JPA
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoSolicitudTipo getTipo() {
        return TipoSolicitudTipo.desdeNombre(nombre);
    }
}
