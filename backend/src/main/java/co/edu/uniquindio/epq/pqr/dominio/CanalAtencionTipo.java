package co.edu.uniquindio.epq.pqr.dominio;

import java.util.Arrays;

/**
 * Canales de atención. El nombre coincide con los registros de la tabla {@code canal_atencion}.
 */
public enum CanalAtencionTipo {

    WEB("Web"),
    PRESENCIAL("Presencial"),
    TELEFONICO("Telefónico"),
    CORREO("Correo electrónico");

    private final String nombre;

    CanalAtencionTipo(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public static CanalAtencionTipo desdeNombre(String nombre) {
        return Arrays.stream(values())
                .filter(canal -> canal.nombre.equalsIgnoreCase(nombre.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Canal de atención desconocido: " + nombre));
    }
}
