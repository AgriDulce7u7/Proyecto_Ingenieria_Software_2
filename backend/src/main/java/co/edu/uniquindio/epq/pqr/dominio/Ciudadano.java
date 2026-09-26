package co.edu.uniquindio.epq.pqr.dominio;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ciudadano")
public class Ciudadano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tipo_documento", nullable = false, length = 5)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(length = 150)
    private String correo;

    @Column(length = 20)
    private String telefono;

    @Column(length = 200)
    private String direccion;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    protected Ciudadano() {
        // Requerido por JPA
    }

    public static Ciudadano registrar(String tipoDocumento, String numeroDocumento, String nombreCompleto,
                                      String correo, String telefono, String direccion,
                                      LocalDateTime fechaRegistro) {
        Ciudadano ciudadano = new Ciudadano();
        ciudadano.tipoDocumento = tipoDocumento;
        ciudadano.numeroDocumento = numeroDocumento;
        ciudadano.nombreCompleto = nombreCompleto;
        ciudadano.correo = correo;
        ciudadano.telefono = telefono;
        ciudadano.direccion = direccion;
        ciudadano.fechaRegistro = fechaRegistro;
        return ciudadano;
    }

    /** Actualiza los datos de contacto con la información más reciente reportada por el ciudadano. */
    public void actualizarContacto(String nombreCompleto, String correo, String telefono, String direccion) {
        this.nombreCompleto = valorOActual(nombreCompleto, this.nombreCompleto);
        this.correo = valorOActual(correo, this.correo);
        this.telefono = valorOActual(telefono, this.telefono);
        this.direccion = valorOActual(direccion, this.direccion);
    }

    private static String valorOActual(String nuevo, String actual) {
        return nuevo == null || nuevo.isBlank() ? actual : nuevo.trim();
    }

    public Integer getId() {
        return id;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
