package co.edu.uniquindio.epq.cartera.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CuotaResponse(int numeroCuota, BigDecimal valor, LocalDate fechaVencimiento, String estado,
                            LocalDateTime fechaPago, String canalPago, String referenciaPago) {
}
