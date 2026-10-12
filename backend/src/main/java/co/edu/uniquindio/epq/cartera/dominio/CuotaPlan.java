package co.edu.uniquindio.epq.cartera.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cuota calculada por la política de cuotas (antes de registrar el acuerdo). */
public record CuotaPlan(int numero, BigDecimal valor, LocalDate fechaVencimiento) {
}
