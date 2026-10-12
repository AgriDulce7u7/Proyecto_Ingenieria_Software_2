package co.edu.uniquindio.epq.comercial.dominio;

import java.util.Arrays;

/**
 * Servicios públicos que presta EPQ. El nombre coincide con la tabla {@code servicio}.
 */
public enum TipoServicio {

    ACUEDUCTO("Acueducto", "ACU", true),
    ALCANTARILLADO("Alcantarillado", "ALC", false),
    GAS("Gas", "GAS", true);

    private final String nombre;
    private final String prefijoContrato;
    private final boolean requiereMedidor;

    TipoServicio(String nombre, String prefijoContrato, boolean requiereMedidor) {
        this.nombre = nombre;
        this.prefijoContrato = prefijoContrato;
        this.requiereMedidor = requiereMedidor;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPrefijoContrato() {
        return prefijoContrato;
    }

    /** SWR-19: acueducto y gas requieren número de medidor para activar el contrato. */
    public boolean requiereMedidor() {
        return requiereMedidor;
    }

    public static TipoServicio desdeNombre(String nombre) {
        return Arrays.stream(values())
                .filter(t -> t.nombre.equalsIgnoreCase(nombre))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Servicio desconocido: " + nombre));
    }
}
