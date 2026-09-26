package co.edu.uniquindio.epq.facturacion.web.dto;

import jakarta.validation.constraints.Pattern;

/**
 * Solicitud de generación manual de un lote.
 *
 * @param periodo periodo de consumo a facturar con formato {@code YYYY-MM}; si se omite se usa el mes anterior
 */
public record GenerarLoteRequest(
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "El periodo debe tener el formato YYYY-MM")
        String periodo) {
}
