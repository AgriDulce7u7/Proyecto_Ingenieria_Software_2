package co.edu.uniquindio.epq.pqr.dominio;

import java.util.Arrays;

/**
 * Tipos de solicitud. El nombre coincide con los registros de la tabla {@code tipo_solicitud}.
 */
public enum TipoSolicitudTipo {

    PETICION("Petición"),
    QUEJA("Queja"),
    RECLAMO("Reclamo");

    private final String nombre;

    TipoSolicitudTipo(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public static TipoSolicitudTipo desdeNombre(String nombre) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.nombre.equalsIgnoreCase(nombre.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Tipo de solicitud desconocido: " + nombre));
    }
}
