package co.edu.uniquindio.epq.cartera.web.dto;

import java.math.BigDecimal;
import java.util.List;

/** Simulación del acuerdo antes de registrarlo. */
public record PlanCuotasResponse(String numeroContrato, BigDecimal totalDeuda, List<String> facturasIncluidas,
                                 int numeroCuotas, List<CuotaResponse> cuotas) {
}
