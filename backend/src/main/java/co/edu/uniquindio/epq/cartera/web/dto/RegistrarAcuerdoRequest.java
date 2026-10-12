package co.edu.uniquindio.epq.cartera.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * RF-12: acuerdo de pago para las facturas vencidas de un contrato. El número de cuotas y las fechas se
 * calculan automáticamente si no se indican (SWR-30).
 */
public record RegistrarAcuerdoRequest(
        @NotBlank(message = "El número de contrato es obligatorio") String numeroContrato,
        @Min(1) @Max(36) Integer numeroCuotas,
        LocalDate fechaPrimeraCuota,
        @Size(max = 500) String observacion) {
}
