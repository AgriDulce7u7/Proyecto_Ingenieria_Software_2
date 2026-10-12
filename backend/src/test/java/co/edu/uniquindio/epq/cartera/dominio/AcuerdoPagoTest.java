package co.edu.uniquindio.epq.cartera.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.comercial.dominio.EstadoContrato;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion;
import co.edu.uniquindio.epq.pagos.dominio.CanalPago;

class AcuerdoPagoTest {

    private final LocalDateTime ahora = LocalDateTime.of(2026, 10, 11, 9, 0);

    private AcuerdoPago acuerdo() {
        PlanCuotas plan = new PlanCuotas(new BigDecimal("100000"), List.of(
                new CuotaPlan(1, new BigDecimal("50000"), LocalDate.of(2026, 11, 10)),
                new CuotaPlan(2, new BigDecimal("50000"), LocalDate.of(2026, 12, 10))));
        return AcuerdoPago.registrar("AP-2026-0003", DatosPruebaFacturacion.contrato(EstadoContrato.ACTIVO), plan,
                null, "cartera", ahora);
    }

    @Test
    void lasCuotasSePaganEnOrdenYPorSuValorExacto() {
        AcuerdoPago acuerdo = acuerdo();

        assertThrows(ReglaNegocioException.class,
                () -> acuerdo.pagarCuota(2, new BigDecimal("50000"), CanalPago.PRESENCIAL, "R1", ahora));
        assertThrows(ReglaNegocioException.class,
                () -> acuerdo.pagarCuota(1, new BigDecimal("40000"), CanalPago.PRESENCIAL, "R1", ahora));

        assertFalse(acuerdo.pagarCuota(1, new BigDecimal("50000"), CanalPago.PRESENCIAL, "R1", ahora));
        assertEquals(0, new BigDecimal("50000").compareTo(acuerdo.saldoPendiente()));
        assertTrue(acuerdo.pagarCuota(2, null, CanalPago.EN_LINEA, "R2", ahora));
        assertEquals(EstadoAcuerdo.CUMPLIDO, acuerdo.getEstado());
    }

    @Test
    void rn07UnaCuotaVencidaInvalidaElAcuerdo() {
        AcuerdoPago acuerdo = acuerdo();
        LocalDate hoy = LocalDate.of(2026, 11, 11);

        assertFalse(acuerdo.tieneCuotaVencida(LocalDate.of(2026, 11, 10))); // vence ese día, aún no está vencida
        assertTrue(acuerdo.tieneCuotaVencida(hoy));

        acuerdo.marcarIncumplido(hoy, ahora);
        assertEquals(EstadoAcuerdo.INCUMPLIDO, acuerdo.getEstado());
        assertEquals(EstadoCuota.VENCIDA, acuerdo.getCuotas().get(0).getEstado());
        assertEquals(EstadoCuota.PENDIENTE, acuerdo.getCuotas().get(1).getEstado());
        assertThrows(ReglaNegocioException.class,
                () -> acuerdo.pagarCuota(1, null, CanalPago.PRESENCIAL, "R1", ahora));
    }
}
