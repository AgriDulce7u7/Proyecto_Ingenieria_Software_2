package co.edu.uniquindio.epq.comercial.dominio;

import java.util.Objects;

/**
 * Cambio de un campo de un usuario del servicio (SWR-15: auditoría de cada cambio).
 */
public record CambioDato(String campo, String valorAnterior, String valorNuevo) {

    public boolean esCambio() {
        return !Objects.equals(valorAnterior, valorNuevo);
    }

    @Override
    public String toString() {
        return "%s: '%s' → '%s'".formatted(campo, valorAnterior == null ? "" : valorAnterior,
                valorNuevo == null ? "" : valorNuevo);
    }
}
