package co.edu.uniquindio.epq.pagos.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DS-03 paso 6: datos del medio de pago (validación básica de formato, CO-02). No se almacenan.
 *
 * @param valor opcional; si se envía debe coincidir con el total de la factura
 */
public record PagoEnLineaRequest(
        @NotBlank @Pattern(regexp = "\\d{13,19}", message = "El número de tarjeta debe tener entre 13 y 19 dígitos")
        String numeroTarjeta,
        @NotBlank @Size(max = 100) String titular,
        @NotNull @Min(1) @Max(12) Integer mesVencimiento,
        @NotNull @Min(2000) @Max(2100) Integer anioVencimiento,
        @NotBlank @Pattern(regexp = "\\d{3,4}", message = "El código de seguridad debe tener 3 o 4 dígitos") String cvv,
        BigDecimal valor) {

    @Override
    public String toString() {
        return "PagoEnLineaRequest[tarjeta terminada en "
                + (numeroTarjeta == null || numeroTarjeta.length() < 4 ? "?" : numeroTarjeta.substring(numeroTarjeta.length() - 4))
                + "]";
    }
}
