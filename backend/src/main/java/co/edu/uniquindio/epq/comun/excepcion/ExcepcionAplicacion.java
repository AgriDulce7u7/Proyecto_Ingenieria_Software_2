package co.edu.uniquindio.epq.comun.excepcion;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

/**
 * Excepción de aplicación que conoce su propio código HTTP, título y datos adicionales.
 *
 * <p>El manejador global la traduce de forma genérica a {@code ProblemDetail}: agregar un nuevo tipo
 * de error no obliga a modificar el manejador (OCP).</p>
 */
public abstract class ExcepcionAplicacion extends RuntimeException {

    private final transient Map<String, Object> propiedades = new LinkedHashMap<>();

    protected ExcepcionAplicacion(String mensaje) {
        super(mensaje);
    }

    protected ExcepcionAplicacion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public abstract HttpStatus estadoHttp();

    public abstract String titulo();

    /** Agrega un dato adicional que viajará en la respuesta de error (p. ej. el perfil existente). */
    protected final void agregarPropiedad(String nombre, Object valor) {
        propiedades.put(nombre, valor);
    }

    public Map<String, Object> propiedades() {
        return Collections.unmodifiableMap(propiedades);
    }
}
