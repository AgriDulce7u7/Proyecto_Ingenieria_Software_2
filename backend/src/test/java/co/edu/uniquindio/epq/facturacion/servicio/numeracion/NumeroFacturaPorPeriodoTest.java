package co.edu.uniquindio.epq.facturacion.servicio.numeracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.facturacion.dominio.Contrato;

class NumeroFacturaPorPeriodoTest {

    @Test
    void numeroDependeDelPeriodoYDelContrato() {
        Contrato contrato = mock(Contrato.class);
        when(contrato.getId()).thenReturn(7);
        assertEquals("FAC-202608-000007", new NumeroFacturaPorPeriodo().generar(YearMonth.of(2026, 8), contrato));
    }
}
