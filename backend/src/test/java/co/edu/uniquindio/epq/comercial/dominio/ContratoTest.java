package co.edu.uniquindio.epq.comercial.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.excepcion.TransicionEstadoInvalidaException;
import co.edu.uniquindio.epq.facturacion.DatosPruebaFacturacion;

/** Patrón State del contrato (PENDIENTE → ACTIVO ⇄ SUSPENDIDO → CANCELADO) y regla SWR-19. */
class ContratoTest {

    private final LocalDateTime ahora = LocalDateTime.of(2026, 10, 11, 9, 0);

    @Test
    void swr19UnServicioMedidoNoSeActivaSinMedidor() {
        Contrato acueducto = DatosPruebaFacturacion.contrato(EstadoContrato.PENDIENTE);

        assertThrows(ReglaNegocioException.class, () -> acueducto.activar(false));
        assertEquals(EstadoContrato.PENDIENTE, acueducto.getEstado());

        acueducto.activar(true);
        assertEquals(EstadoContrato.ACTIVO, acueducto.getEstado());
        assertTrue(acueducto.esFacturable());
    }

    @Test
    void alcantarilladoNoRequiereMedidor() {
        Contrato alcantarillado = DatosPruebaFacturacion.contrato(EstadoContrato.PENDIENTE);
        ReflectionTestUtils.setField(alcantarillado, "servicio", DatosPruebaFacturacion.servicio("Alcantarillado"));

        alcantarillado.activar(false);
        assertEquals(EstadoContrato.ACTIVO, alcantarillado.getEstado());
    }

    @Test
    void suspenderYReactivarLimpiaLaMarcaDeCandidato() {
        Contrato contrato = DatosPruebaFacturacion.contrato(EstadoContrato.ACTIVO);
        assertTrue(contrato.actualizarCandidatoSuspension(true, ahora));
        assertFalse(contrato.actualizarCandidatoSuspension(true, ahora)); // sin cambio

        contrato.suspender();
        assertEquals(EstadoContrato.SUSPENDIDO, contrato.getEstado());
        assertFalse(contrato.isCandidatoSuspension());
        assertFalse(contrato.esFacturable());

        contrato.activar(true);
        assertEquals(EstadoContrato.ACTIVO, contrato.getEstado());
    }

    @Test
    void unContratoCanceladoNoAdmiteTransiciones() {
        Contrato contrato = DatosPruebaFacturacion.contrato(EstadoContrato.ACTIVO);
        contrato.cancelar();

        assertThrows(TransicionEstadoInvalidaException.class, () -> contrato.activar(true));
        assertThrows(TransicionEstadoInvalidaException.class, contrato::suspender);
        assertThrows(TransicionEstadoInvalidaException.class, contrato::cancelar);
    }

    @Test
    void unContratoPendienteNoSePuedeSuspender() {
        Contrato contrato = DatosPruebaFacturacion.contrato(EstadoContrato.PENDIENTE);
        assertThrows(TransicionEstadoInvalidaException.class, contrato::suspender);
    }
}
