package co.edu.uniquindio.epq.cartera.web.dto;

import java.math.BigDecimal;

import co.edu.uniquindio.epq.pagos.dominio.CanalPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @param valor opcional; si se envía debe coincidir con el valor de la cuota
 */
public record PagarCuotaRequest(@NotNull(message = "El canal es obligatorio") CanalPago canal,
                                @Size(max = 100) String referencia,
                                BigDecimal valor) {
}
