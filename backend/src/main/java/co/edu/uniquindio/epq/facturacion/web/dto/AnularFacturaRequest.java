package co.edu.uniquindio.epq.facturacion.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnularFacturaRequest(
        @NotBlank(message = "El motivo de la anulación es obligatorio") @Size(min = 10, max = 500) String motivo) {
}
