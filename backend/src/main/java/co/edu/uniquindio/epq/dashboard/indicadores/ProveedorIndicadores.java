package co.edu.uniquindio.epq.dashboard.indicadores;

import java.time.LocalDate;
import java.util.List;

import co.edu.uniquindio.epq.dashboard.web.dto.IndicadorResponse;

/**
 * Patrón <b>Strategy</b> + <b>Composite</b>: cada proveedor calcula los indicadores de una categoría y el
 * dashboard los compone. Agregar una categoría es agregar un proveedor (OCP).
 */
public interface ProveedorIndicadores {

    /** Posición de la categoría en el dashboard. */
    int orden();

    String categoria();

    List<IndicadorResponse> calcular(LocalDate hoy);
}
