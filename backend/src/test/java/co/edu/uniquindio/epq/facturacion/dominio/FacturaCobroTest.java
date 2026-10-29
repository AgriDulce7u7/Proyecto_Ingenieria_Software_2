package co.edu.uniquindio.epq.facturacion.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.excepcion.TransicionEstadoInvalidaException;
import co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion;

/** Ciclo de cobro de la factura (DE-01): PENDIENTE → VENCIDA/PAGADA/ANULADA. */
class FacturaCobroTest {

    private final LocalDateTime ahora = LocalDateTime.of(2026, 10, 11, 10, 0);
    private final LocalDate despuesDelVencimiento = DatosPruebaFacturacion.FECHA_VENCIMIENTO.plusDays(1);

    @Test
    void unaFacturaEmitidaNacePendiente() {
        assertEquals(EstadoCobroFactura.PENDIENTE, DatosPruebaFacturacion.factura().getEstadoCobro());
    }

    @Test
    void venceSoloDespuesDeLaFechaDeVencimiento() {
        Factura factura = DatosPruebaFacturacion.factura();

        assertFalse(factura.vencerSiCorresponde(DatosPruebaFacturacion.FECHA_VENCIMIENTO));
        assertTrue(factura.vencerSiCorresponde(despuesDelVencimiento));
        assertEquals(EstadoCobroFactura.VENCIDA, factura.getEstadoCobro());
        assertTrue(factura.estaEnMora());
    }

    @Test
    void swr23NoSeAdmiteUnSegundoPago() {
        Factura factura = DatosPruebaFacturacion.factura();
        factura.registrarPago(ahora);

        assertEquals(EstadoCobroFactura.PAGADA, factura.getEstadoCobro());
        assertEquals(ahora, factura.getFechaPago());
        assertThrows(ReglaNegocioException.class, () -> factura.registrarPago(ahora));
    }

    @Test
    void unaFacturaVencidaTambienSePuedePagar() {
        Factura factura = DatosPruebaFacturacion.factura();
        factura.vencerSiCorresponde(despuesDelVencimiento);
        factura.registrarPago(ahora);
        assertEquals(EstadoCobroFactura.PAGADA, factura.getEstadoCobro());
    }

    @Test
    void co02ElPagoDebeSerPorElTotalDeLaFactura() {
        Factura factura = DatosPruebaFacturacion.factura();
        BigDecimal total = DatosPruebaFacturacion.LIQUIDACION.total();

        factura.validarPago(total);
        factura.validarPago(new BigDecimal("81783.250"));   // se compara el monto, no la escala
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> factura.validarPago(new BigDecimal("81783.24")));
        assertTrue(error.getMessage().contains("no coincide con el total"));
        assertEquals(EstadoCobroFactura.PENDIENTE, factura.getEstadoCobro());   // validar no cambia el estado
    }

    @Test
    void validarPagoRevisaPrimeroElEstadoDeLaFactura() {
        Factura factura = DatosPruebaFacturacion.factura();
        factura.registrarPago(ahora);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> factura.validarPago(BigDecimal.ONE));
        assertTrue(error.getMessage().contains("ya se encuentra pagada"));
    }

    @Test
    void validarPagoRechazaUnaFacturaEnAcuerdoAunqueElValorSeaElTotal() {
        Factura factura = DatosPruebaFacturacion.factura();
        factura.vencerSiCorresponde(despuesDelVencimiento);
        factura.incluirEnAcuerdo(2);

        assertThrows(ReglaNegocioException.class, () -> factura.validarPago(DatosPruebaFacturacion.LIQUIDACION.total()));
    }

    @Test
    void laAnulacionExigeMotivoYBloqueaPagos() {
        Factura factura = DatosPruebaFacturacion.factura();

        assertThrows(ReglaNegocioException.class, () -> factura.anular("  ", "admin", ahora));
        factura.anular("Lectura mal registrada", "admin", ahora);

        assertEquals(EstadoCobroFactura.ANULADA, factura.getEstadoCobro());
        assertThrows(ReglaNegocioException.class, () -> factura.registrarPago(ahora));
    }

    @Test
    void unaFacturaPagadaNoSePuedeAnular() {
        Factura factura = DatosPruebaFacturacion.factura();
        factura.registrarPago(ahora);
        assertThrows(TransicionEstadoInvalidaException.class, () -> factura.anular("Error", "admin", ahora));
    }

    @Test
    void rn07AcuerdoDePagoCubreLaFacturaYAlIncumplirseVuelveAMora() {
        Factura factura = DatosPruebaFacturacion.factura();
        assertThrows(ReglaNegocioException.class, () -> factura.incluirEnAcuerdo(1)); // aún no vence

        factura.vencerSiCorresponde(despuesDelVencimiento);
        factura.incluirEnAcuerdo(1);
        assertEquals(EstadoCobroFactura.PENDIENTE, factura.getEstadoCobro());
        assertFalse(factura.estaEnMora());
        assertFalse(factura.vencerSiCorresponde(despuesDelVencimiento.plusDays(30))); // protegida por el acuerdo
        assertThrows(ReglaNegocioException.class, () -> factura.registrarPago(ahora)); // se paga por cuotas

        factura.liberarDeAcuerdo();
        assertEquals(EstadoCobroFactura.VENCIDA, factura.getEstadoCobro());
        assertTrue(factura.estaEnMora());
    }

    @Test
    void alCumplirseElAcuerdoLaFacturaQuedaPagada() {
        Factura factura = DatosPruebaFacturacion.factura();
        factura.vencerSiCorresponde(despuesDelVencimiento);
        factura.incluirEnAcuerdo(7);

        factura.marcarPagadaPorAcuerdo(ahora);
        assertEquals(EstadoCobroFactura.PAGADA, factura.getEstadoCobro());
    }
}
