package co.edu.uniquindio.epq.comercial.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @param criterio descripción de a qué predios aplica (p. ej. "Estrato 2 · Salento")
 */
public record TarifaResponse(
        Integer id,
        String nombre,
        String servicio,
        BigDecimal valorPorUnidad,
        BigDecimal cargoFijo,
        String municipio,
        Integer estrato,
        String criterio,
        LocalDate vigenciaInicio,
        LocalDate vigenciaFin) {
}
