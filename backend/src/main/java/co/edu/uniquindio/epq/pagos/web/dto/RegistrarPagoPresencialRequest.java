package co.edu.uniquindio.epq.pagos.web.dto;

import java.math.BigDecimal;

import co.edu.uniquindio.epq.pagos.dominio.CanalPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * SyR-05a: pago registrado por un funcionario. Canal PRESENCIAL (caja) o BANCO (consignación).
 *
 * @param valor      debe coincidir con el total de la factura (no se admiten pagos parciales)
 * @param referencia número de recibo de caja o de la consignación bancaria
 */
public record RegistrarPagoPresencialRequest(
        @NotBlank(message = "El número de factura es obligatorio") String numeroFactura,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal valor,
        @NotNull(message = "El canal es obligatorio (PRESENCIAL o BANCO)") CanalPago canal,
        @Size(max = 100) String referencia) {
}
